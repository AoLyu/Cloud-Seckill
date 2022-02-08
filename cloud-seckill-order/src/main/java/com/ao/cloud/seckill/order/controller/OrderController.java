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
import java.util.concurrent.*;


@RestController
@RequestMapping("/order")
public class OrderController  {
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemFeignClient itemFeignClient;

    private ExecutorService executorService;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(20);
        orderCreateRateLimiter = RateLimiter.create(300);
    }

    //生成验证码
    @RequestMapping(value = "/generateverifycode",method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void generateverifycode(HttpServletResponse response) throws CloudSekillException, IOException {
//        String token = httpServletRequest.getParameterMap().get("token")[0];
//        if(StringUtils.isEmpty(token)){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.USER_NOT_LOGIN.getCode(),"用户还未登陆，不能生成验证码");
//        }
        //未登录时，网关拦截了
//        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
//        if(userModel == null){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.USER_NOT_LOGIN.getCode(),"用户还未登陆，不能生成验证码");
//        }
        //从header中获取uerId;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("user_id");
        String id =httpServletRequest.getHeader("user_id");

        Map<String,Object> map = CodeUtil.generateCodeAndPic();

        redisTemplate.opsForValue().set("verify_code_"+userId,map.get("code"));
        redisTemplate.expire("verify_code_"+userId,10,TimeUnit.MINUTES);

        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
    }

    //生成秒杀令牌
    @PostMapping(value = "/generatetoken")
    public ApiRestResponse generatetoken(@RequestParam(name="itemId")Integer itemId,
                                         @RequestParam(name="promoId")Integer promoId,
                                         @RequestParam(name="verifyCode")String verifyCode) throws CloudSekillException {
        //根据token获取用户信息
//        String token = httpServletRequest.getParameterMap().get("token")[0];
//        if(StringUtils.isEmpty(token)){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.USER_NOT_LOGIN.getCode(),"用户还未登陆，不能下单");
//        }
        //未登录网关拦截了
        //获取用户的登陆信息
//        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
//        if(userModel == null){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.USER_NOT_LOGIN.getCode(),"用户还未登陆，不能下单");
//        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("user_id");

        //通过verifycode验证验证码的有效性
        String redisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_"+userId);
        if(StringUtils.isEmpty(redisVerifyCode)){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"请求非法");
        }
        if(!redisVerifyCode.equalsIgnoreCase(verifyCode)){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"请求非法，验证码错误");
        }
        //获取秒杀访问令牌
        String promoToken = itemFeignClient.generateSecondKillTokenByFeign(promoId,itemId);

        if(promoToken == null){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"生成令牌失败");
        }
        //返回对应的结果
        return ApiRestResponse.success(promoToken);
    }
        //封装下单请求
    @PostMapping(value = "/createorder")
    public ApiRestResponse createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId,
                                        @RequestParam(name="promoToken",required = false)String promoToken) throws CloudSekillException {

        if(!orderCreateRateLimiter.tryAcquire()){
            throw new CloudSekillException(CloudSeckillExceptionEnum.RATELIMIT);
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("user_id");
        //未登录的用户，网关拦截了
//        String token = httpServletRequest.getParameterMap().get("token")[0];
//        if(StringUtils.isEmpty(token)){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.USER_NOT_LOGIN.getCode(),"用户还未登陆，不能下单");
//        }
        //获取用户的登陆信息
//        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
//        if(userModel == null){
//            throw new CloudSekillException(CloudSeckillExceptionEnum.USER_NOT_LOGIN.getCode(),"用户还未登陆，不能下单");
//        }
        //校验秒杀令牌是否正确
        if(promoId != null){
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId);
            if(inRedisPromoToken == null){
                throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"秒杀令牌校验失败");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"秒杀令牌校验失败");
            }
        }
        //同步调用线程池的submit方法
        //拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //加入库存流水init状态
                String stockLogId = itemFeignClient.initStockLogByFeign(itemId,amount);
                //再去完成对应的下单事务型消息机制
                if(!mqProducer.transactionAsyncReduceStock(Integer.parseInt(userId) ,itemId,promoId,amount,stockLogId)){
                    throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR.getCode(),"下单失败");
                }
                return null;
            }
        });
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR);
        }
        return ApiRestResponse.success(null);
    }

    @PostMapping("/createorderByFeign")
    public OrderModel createOrderByFeign(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) {
        return orderService.createOrder(userId,itemId,promoId,amount,stockLogId);
    }
}
