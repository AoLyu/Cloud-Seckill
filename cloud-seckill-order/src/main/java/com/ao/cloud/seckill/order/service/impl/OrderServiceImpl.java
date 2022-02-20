package com.ao.cloud.seckill.order.service.impl;


import com.ao.cloud.seckill.common.exception.CloudSeckillExceptionEnum;
import com.ao.cloud.seckill.common.exception.CloudSekillException;
import com.ao.cloud.seckill.order.feign.ItemFeignClient;
import com.ao.cloud.seckill.order.model.dao.OrderDOMapper;
import com.ao.cloud.seckill.order.model.dao.SequenceDOMapper;
import com.ao.cloud.seckill.order.model.dao.StockLogDOMapper;
import com.ao.cloud.seckill.order.model.dataobject.OrderDO;
import com.ao.cloud.seckill.order.model.dataobject.SequenceDO;
import com.ao.cloud.seckill.order.model.dataobject.StockLogDO;
import com.ao.cloud.seckill.order.model.pojo.OrderModel;
import com.ao.cloud.seckill.order.mq.MqProducer;
import com.ao.cloud.seckill.order.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemFeignClient itemFeignClient;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public OrderModel createOrderOld(Integer userId, Integer itemId, Integer promoId, Integer amount) throws CloudSekillException {
        //1.校验下单状态,下单的商品是否存在，用户是否合法，购买数量是否正确
        // Feign调用物品服务扣减库存并返回当前价格，如果扣减不成功，返回null。
        Object currentPrice = itemFeignClient.decreaseStockByItemIdByFeign(itemId,promoId,amount).getData();
        if(!(currentPrice instanceof BigDecimal)) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"订单创建失败");
        }
        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);

        orderModel.setItemPrice((BigDecimal) currentPrice);
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号,订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //4.返回前端
        return orderModel;
    }

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws CloudSekillException {
        //1.校验下单状态,下单的商品是否存在，用户是否合法，购买数量是否正确
        //ItemModel itemModel = itemService.getItemById(itemId);
        // Feign远程获取物品当前价格。
        Object currentPrice = itemFeignClient.getCurrentPriceByItemIdByFeign(itemId,promoId).getData();
        if(!(currentPrice instanceof BigDecimal)) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"订单创建失败");
        }

        if(amount <= 0 || amount > 99){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"数量信息不正确");
        }
        //2.落单减库存（从Redis里面减） // 数量不够就抛异常
        boolean result = this.decreaseStock(itemId,amount);

        if(!result) {
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR.getCode(),"商品已抢购完");
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);

        orderModel.setItemPrice((BigDecimal) currentPrice);
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号,订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //设置库存流水状态为成功
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if(stockLogDO == null){
            throw new CloudSekillException(CloudSeckillExceptionEnum.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

        //使用了事务型消息不再发送普通消息
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                @Override
//                public void afterCommit(){
//                    //异步更新库存
//                    boolean mqResult =  mqProducer.asyncReduceStock(itemId,amount);
////                    if(!mqResult){
////                        itemService.increaseStock(itemId,amount);
////                        throw new BusinessException(EmBusinessError.MQ_SEND_FAIL);
////                    }
//                }
//
//        });

        //4.返回前端
        return orderModel;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo(){
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

    //初始化对应的库存流水
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setStatus(1);
        int res = stockLogDOMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }

    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) {
        //int affectedRow =  itemStockDOMapper.decreaseStock(itemId,amount);
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue() * -1);
        if(result >0){
            //更新库存成功
            return true;
        }else if(result == 0){
            //打上库存已售罄的标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_"+itemId,"true");

            //更新库存成功
            return true;
        }else{
            //更新库存失败
            increaseStock(itemId,amount);
            return false;
        }

    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
        return true;
    }


    @Override
    public StockLogDO getStockLogDOById(String stockLogId){
        return stockLogDOMapper.selectByPrimaryKey(stockLogId);
    }

    @Override
    public int updateStockLogDO(StockLogDO record){
        return  stockLogDOMapper.updateByPrimaryKeySelective(record);
    }
}
