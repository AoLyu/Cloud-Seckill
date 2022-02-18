package com.ao.cloud.seckill.item.service.impl;

import com.ao.cloud.seckill.item.model.dao.PromoDOMapper;
import com.ao.cloud.seckill.item.model.dataobject.PromoDO;
import com.ao.cloud.seckill.item.model.pojo.ItemModel;
import com.ao.cloud.seckill.item.model.pojo.PromoModel;
import com.ao.cloud.seckill.item.service.ItemService;
import com.ao.cloud.seckill.item.service.PromoService;
import io.swagger.models.auth.In;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        //dataobject->model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel == null){
            return null;
        }
        //判断当前时间是否秒杀活动即将开始或正在进行
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0){
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());

        //将库存同步到redis内
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(), itemModel.getStock());

        //将大闸的限制数字设到redis内
        redisTemplate.opsForValue().set("promo_door_count_"+promoId,itemModel.getStock() * 5);

    }

    @Override
    public Boolean validate(Integer promoId, Integer itemId) {
        //判断是否库存已售罄，若对应的售罄key存在，则直接返回下单失败
        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            return false;
        }

        //秒杀是否存在
        PromoModel promoModel = this.getPromoModelByIdInCache(promoId);
        if(promoModel == null){
            return false;
        }

        //判断当前时间是否秒杀活动即将开始或正在进行
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        //判断活动是否正在进行
        if(promoModel.getStatus().intValue() != 2){
            return false;
        }
        //判断item信息是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            return false;
        }
        return true;
    }

    @Override
    public PromoModel getPromoModelByIdInCache(Integer promoId) {
        PromoModel promoModel = (PromoModel) redisTemplate.opsForValue().get("promo_validate_"+promoId);
        if(promoModel == null){
            PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
            //dataobject->model
            promoModel = convertFromDataObject(promoDO);
            if(promoModel == null){
                return null;
            } else {
                redisTemplate.opsForValue().set("promo_validate_" + promoId, promoModel);
                redisTemplate.expire("promo_validate_" + promoId, 10, TimeUnit.MINUTES);
            }
        }
        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
