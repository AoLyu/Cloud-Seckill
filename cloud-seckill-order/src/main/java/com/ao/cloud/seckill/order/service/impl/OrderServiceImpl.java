package com.ao.cloud.seckill.order.service.impl;


import com.ao.cloud.seckill.common.exception.CloudSeckillExceptionEnum;
import com.ao.cloud.seckill.common.exception.CloudSekillException;
import com.ao.cloud.seckill.item.model.dataobject.StockLogDO;
import com.ao.cloud.seckill.item.model.pojo.ItemModel;
import com.ao.cloud.seckill.order.feign.ItemFeignClient;
import com.ao.cloud.seckill.order.model.dao.OrderDOMapper;
import com.ao.cloud.seckill.order.model.dao.SequenceDOMapper;
import com.ao.cloud.seckill.order.model.dataobject.OrderDO;
import com.ao.cloud.seckill.order.model.dataobject.SequenceDO;
import com.ao.cloud.seckill.order.model.pojo.OrderModel;
import com.ao.cloud.seckill.order.mq.MqProducer;
import com.ao.cloud.seckill.order.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemFeignClient itemFeignClient;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws CloudSekillException {
        //1.校验下单状态,下单的商品是否存在，用户是否合法，购买数量是否正确
        //ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemFeignClient.getItemByIdInCacheByFeign(itemId);
        if(itemModel == null){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"商品信息不存在");
        }

        if(amount <= 0 || amount > 99){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"数量信息不正确");
        }

        //2.落单减库存
        boolean result = itemFeignClient.decreaseStockByFeign(itemId,amount);
        if(!result){
            throw new CloudSekillException(CloudSeckillExceptionEnum.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号,订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //加上商品的销量
        itemFeignClient.increaseSalesByFeign(itemId,amount);

        //设置库存流水状态为成功
        StockLogDO stockLogDO = itemFeignClient.getStockLogDOByIdByFeign(stockLogId);
        if(stockLogDO == null){
            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(2);
        itemFeignClient.updateStockLogDOByFeign(stockLogDO);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit(){
                    //异步更新库存
                    boolean mqResult =  mqProducer.asyncReduceStock(itemId,amount);
//                    if(!mqResult){
//                        itemService.increaseStock(itemId,amount);
//                        throw new BusinessException(EmBusinessError.MQ_SEND_FAIL);
//                    }
                }

        });

        //4.返回前端
        return orderModel;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderNo(){
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);

        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO =  sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i = 0; i < 6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);


        //最后2位为分库分表位,暂时写死
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
