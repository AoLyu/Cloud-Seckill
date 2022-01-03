package com.ao.cloud.seckill.order.feign;

import com.ao.cloud.seckill.item.model.dataobject.StockLogDO;
import com.ao.cloud.seckill.item.model.pojo.ItemModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "cloud-seckill-item")
public interface ItemFeignClient {

    @PostMapping("/generateSecondKillTokenByFeign")
    String generateSecondKillTokenByFeign(Integer promoId,Integer itemId,Integer userId);

    @PostMapping("/initStockLogByFeign")
    String initStockLogByFeign(Integer itemId,Integer amount);

    @GetMapping("/getItemByIdInCacheByFeign")
    ItemModel getItemByIdInCacheByFeign(Integer id);

    @PostMapping("/decreaseStockByFeign")
    boolean decreaseStockByFeign(Integer itemId,Integer amount);

    @PostMapping("/increaseSalesByFeign")
    void increaseSalesByFeign(Integer itemId,Integer amount);

    @GetMapping("/getStockLogDOByIdByFeign")
    StockLogDO getStockLogDOByIdByFeign(String stockLogId);

    @PostMapping("/updateStockLogDOByFeign")
    int updateStockLogDOByFeign(StockLogDO record);

}
