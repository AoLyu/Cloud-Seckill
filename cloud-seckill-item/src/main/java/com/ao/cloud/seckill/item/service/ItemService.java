package com.ao.cloud.seckill.item.service;


import com.ao.cloud.seckill.common.exception.CloudSekillException;
import com.ao.cloud.seckill.item.model.pojo.ItemModel;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws CloudSekillException;

    //商品列表浏览
    List<ItemModel> listItem();

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //item及promo model缓存模型
    ItemModel getItemByIdInCache(Integer id);

    //查询库存
    int getStock(Integer itemId);

    @Transactional
    void decreaseStockOld(Integer itemId, Integer amount) throws CloudSekillException;

    //库存扣减
    boolean decreaseStock(Integer itemId,Integer amount)throws CloudSekillException;

    //库存回补
    boolean increaseStock(Integer itemId,Integer amount)throws CloudSekillException;


    //商品销量增加
    void increaseSales(Integer itemId,Integer amount)throws CloudSekillException;

}
