package com.ao.cloud.seckill.item.controller;

import com.ao.cloud.seckill.common.exception.CloudSekillException;
import com.ao.cloud.seckill.common.response.ApiRestResponse;
import com.ao.cloud.seckill.item.model.dataobject.StockLogDO;
import com.ao.cloud.seckill.item.model.pojo.ItemModel;
import com.ao.cloud.seckill.item.model.vo.ItemVO;
import com.ao.cloud.seckill.item.service.CacheService;
import com.ao.cloud.seckill.item.service.ItemService;
import com.ao.cloud.seckill.item.service.PromoService;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RestController
public class ItemController  {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private PromoService promoService;

    //创建商品的controller
    @PostMapping(value = "/create")
    public ApiRestResponse createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price")BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws CloudSekillException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);

        return ApiRestResponse.success(itemVO);
    }

    @PostMapping(value = "/getone")
    public ApiRestResponse getOne(){
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("user_id");
        return ApiRestResponse.success("Current User Id is:"+userId);
    }

    @PostMapping(value = "/publishpromo")
    public ApiRestResponse publishpromo(@RequestParam(name = "id")Integer id){
        promoService.publishPromo(id);
        return ApiRestResponse.success(null);

    }
    //商品详情页浏览
    @GetMapping(value = "/get")
    public ApiRestResponse getItem(@RequestParam(name = "id")Integer id){
        ItemModel itemModel = null;

        //先取本地缓存
        itemModel = (ItemModel) cacheService.getFromCommonCache("item_"+id);

        if(itemModel == null){
            //根据商品的id到redis内获取
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_"+id);

            //若redis内不存在对应的itemModel,则访问下游service
            if(itemModel == null){
                itemModel = itemService.getItemById(id);
                //设置itemModel到redis内
                redisTemplate.opsForValue().set("item_"+id,itemModel);
                redisTemplate.expire("item_"+id,10, TimeUnit.MINUTES);
            }
            //填充本地缓存
            cacheService.setCommonCache("item_"+id,itemModel);
        }
        ItemVO itemVO = convertVOFromModel(itemModel);

        return ApiRestResponse.success(itemVO);
    }

    //商品列表页面浏览
    @GetMapping(value = "/list")
    public ApiRestResponse listItem(){
        List<ItemModel> itemModelList = itemService.listItem();

        //使用stream apiJ将list内的itemModel转化为ITEMVO;
        List<ItemVO> itemVOList =  itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return ApiRestResponse.success(itemVOList);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        if(itemModel.getPromoModel() != null){
            //有正在进行或即将进行的秒杀活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }

    @PostMapping("generateSecondKillTokenByFeign")
    public String generateSecondKillTokenByFeign(Integer promoId,Integer itemId){

        String userId = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader("user_id");
        return promoService.generateSecondKillToken(promoId,itemId,userId);
    }

    @PostMapping("/initStockLogByFeign")
    public String initStockLogByFeign(Integer itemId,Integer amount){
        return itemService.initStockLog(itemId,amount);
    }

    @GetMapping("/getItemByIdInCacheByFeign")
    public ItemModel getItemByIdInCacheByFeign(Integer id){
        return itemService.getItemByIdInCache(id);
    }

    @PostMapping("/decreaseStockByFeign")
    public boolean decreaseStockByFeign(Integer itemId,Integer amount){
        return itemService.decreaseStock(itemId,amount);
    }

    @PostMapping("/increaseSalesByFeign")
    public void increaseSalesByFeign(Integer itemId,Integer amount){
        itemService.increaseSales(itemId,amount);
    }

    @GetMapping("/getStockLogDOByIdByFeign")
    public StockLogDO getStockLogDOByIdByFeign(String stockLogId){
        return itemService.getStockLogDOById(stockLogId);
    }

    @PostMapping("/updateStockLogDOByFeign")
    public int updateStockLogDOByFeign(StockLogDO record){
        return itemService.updateStockLogDO(record);
    }
}
