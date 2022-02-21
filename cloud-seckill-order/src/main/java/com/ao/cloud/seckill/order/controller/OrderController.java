package com.ao.cloud.seckill.order.controller;

import com.ao.cloud.seckill.common.exception.CloudSeckillExceptionEnum;
import com.ao.cloud.seckill.common.exception.CloudSekillException;
import com.ao.cloud.seckill.common.response.ApiRestResponse;
import com.ao.cloud.seckill.common.util.CodeUtil;
import com.ao.cloud.seckill.order.feign.ItemFeignClient;
import com.ao.cloud.seckill.order.model.pojo.OrderModel;
import com.ao.cloud.seckill.order.mq.MqProducer;
import com.ao.cloud.seckill.order.service.OrderService;
import com.google.common.util.concurrent.RateLimiter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Api("订单模块")
@RestController
public class OrderController  {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemFeignClient itemFeignClient;

//    private ExecutorService executorService;

    private ThreadPoolExecutor threadPoolExecutor;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init(){
//        executorService = Executors.newFixedThreadPool(20);
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(500);
        ThreadFactory nameThreadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        };
        threadPoolExecutor = new ThreadPoolExecutor(20,20,0,TimeUnit.MINUTES,queue,nameThreadFactory);
        orderCreateRateLimiter = RateLimiter.create(200);
    }

    //生成秒杀令牌
    @ApiOperation("获取秒杀令牌")
    @PostMapping("/generateSecondKillToken")
    public ApiRestResponse generateSecondKillToken(@RequestParam(name="itemId")Integer itemId,
                                                   @RequestParam(name="promoId")Integer promoId,
                                                   @RequestParam(name="verifyCode")String verifyCode){
        String userId = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getHeader("user_id");
        Object redisCodeObj = redisTemplate.opsForValue().get("verify_code_"+userId);
        String redisVerifyCode = null;
        if(redisCodeObj!=null)
            redisVerifyCode =redisCodeObj.toString();
        if(StringUtils.isEmpty(redisVerifyCode)){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"验证码错误");
        }
        if(!redisVerifyCode.equalsIgnoreCase(verifyCode)){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"验证码错误");
        }

        Object res = itemFeignClient.validateByFeign( itemId, promoId).getData();

        if(!(res instanceof Boolean && ((Boolean) res).booleanValue()==true)) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"生成令牌失败");
        }
        //获取秒杀大闸的count数量
        long result = redisTemplate.opsForValue().increment("promo_door_count_"+promoId,-1);
        if(result < 0){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"生成令牌失败");
        }

        //生成秒杀令牌，存入redis内并给一个5分钟的有效期
        String promoToken = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,promoToken);
        redisTemplate.expire("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,5, TimeUnit.MINUTES);

        //返回对应的结果
        return ApiRestResponse.success(promoToken);
    }

    //生成验证码
    @ApiOperation("获取防刷验证码")
    @RequestMapping(value = "/generateverifycode",method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void generateverifycode(HttpServletRequest request,HttpServletResponse response) throws CloudSekillException, IOException {

        String userId =request.getHeader("user_id");

        Map<String,Object> map = CodeUtil.generateCodeAndPic();

        redisTemplate.opsForValue().set("verify_code_"+userId,map.get("code"));
        redisTemplate.expire("verify_code_"+userId,10,TimeUnit.MINUTES);

        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
    }

    //封装下单请求
    @ApiOperation("下单")
    @PostMapping(value = "/createorder")
    public ApiRestResponse createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                       @RequestParam(name="promoId")Integer promoId,
                                       @RequestParam(name="promoToken")String promoToken
                                        ) throws CloudSekillException {

        if(!orderCreateRateLimiter.tryAcquire()){
            throw new CloudSekillException(CloudSeckillExceptionEnum.RATELIMIT);
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("user_id");
        //判断是否库存已售罄，若对应的售罄key存在，则直接返回下单失败
        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR.getCode(),"商品已抢完，下单失败");
        }
        //校验秒杀令牌是否正确
        if(promoId != null){
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId);
            if(inRedisPromoToken == null){
                throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"秒杀令牌校验失败,请获取");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"秒杀令牌校验失败，请获取");
            }
        }
//        //加入库存流水init状态
//        String stockLogId = orderService.initStockLog(itemId,amount);
//        //再去完成对应的下单事务型消息机制
//        if(!mqProducer.transactionAsyncReduceStock(Integer.parseInt(userId) ,itemId,amount,promoId,stockLogId)){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR.getCode(),"下单失败");
//        }
//        同步调用线程池的submit方法
//        拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = threadPoolExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //加入库存流水init状态
                String stockLogId = orderService.initStockLog(itemId,amount);
                //再去完成对应的下单事务型消息机制
                if(!mqProducer.transactionAsyncReduceStock(Integer.parseInt(userId) ,itemId,amount,promoId,stockLogId)){
                    throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR.getCode(),"下单失败");
                }
                return null;
            }
        });
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR.getCode(),"下单失败");
        } catch (ExecutionException e) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR.getCode(),"下单失败");
        }
        return ApiRestResponse.success(null);
    }

    //封装下单请求
    @PostMapping(value = "/createorderold")
    public ApiRestResponse createOrderOld(@RequestParam(name="itemId")Integer itemId,
                                       @RequestParam(name="amount")Integer amount,
                                       @RequestParam(name="promoId")Integer promoId,
                                       @RequestParam(name="promoToken")String promoToken
    ) throws CloudSekillException {

//        if(!orderCreateRateLimiter.tryAcquire()){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.RATELIMIT);
//        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("user_id");

        OrderModel orderOld = orderService.createOrderOld(Integer.parseInt(userId),itemId,promoId,amount);
        return ApiRestResponse.success(orderOld);
    }

}
