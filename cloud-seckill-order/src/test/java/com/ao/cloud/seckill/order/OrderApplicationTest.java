package com.ao.cloud.seckill.order;

import com.ao.cloud.seckill.order.model.dao.StockLogDOMapper;
import com.ao.cloud.seckill.order.model.dataobject.StockLogDO;
import com.ao.cloud.seckill.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class OrderApplicationTest {

    @Autowired
    StockLogDOMapper stockLogDOMapper;

    @Autowired
    OrderService orderService;

    @Test
    void testStockLog(){

//        String s = orderService.initStockLog(6, 1);
//        System.out.println(s);
//        StockLogDO stockLogDO = new StockLogDO();
//        stockLogDO.setItemId(6);
//        stockLogDO.setAmount(1);
//        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
//        stockLogDO.setStatus(1);
//        int re = stockLogDOMapper.insertSelective(stockLogDO);
//
//        System.out.println(re);
    }
}
