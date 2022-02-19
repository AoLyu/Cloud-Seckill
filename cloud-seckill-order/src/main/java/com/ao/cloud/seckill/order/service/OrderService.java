package com.ao.cloud.seckill.order.service;


import com.ao.cloud.seckill.common.exception.CloudSekillException;
import com.ao.cloud.seckill.order.model.dataobject.StockLogDO;
import com.ao.cloud.seckill.order.model.pojo.OrderModel;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {
    //使用1,通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws CloudSekillException;

    //初始化对应的库存流水
    String initStockLog(Integer itemId, Integer amount);

    StockLogDO getStockLogDOById(String stockLogId);

    int updateStockLogDO(StockLogDO record);

    boolean increaseStock(Integer itemId, Integer amount);

    boolean decreaseStock(Integer itemId, Integer amount);
}
