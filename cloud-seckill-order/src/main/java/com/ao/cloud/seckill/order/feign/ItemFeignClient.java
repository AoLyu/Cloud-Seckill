package com.ao.cloud.seckill.order.feign;

import com.ao.cloud.seckill.item.model.dataobject.StockLogDO;
import com.ao.cloud.seckill.item.model.pojo.ItemModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "cloud-seckill-item")
public interface ItemFeignClient {
    @PostMapping("/generateSecondKillTokenByFeign")
    String generateSecondKillTokenByFeign(@RequestParam("promoId") Integer promoId,@RequestParam("itemId") Integer itemId);

    @PostMapping("/initStockLogByFeign")
    String initStockLogByFeign(@RequestParam("itemId") Integer itemId,@RequestParam("amount") Integer amount);

    @GetMapping("/getItemByIdInCacheByFeign")
    ItemModel getItemByIdInCacheByFeign(Integer id);

    @PostMapping("/decreaseStockByFeign")
    boolean decreaseStockByFeign(@RequestParam("itemId") Integer itemId,@RequestParam("amount") Integer amount);

    @GetMapping("/getStockLogDOByIdByFeign")
    StockLogDO getStockLogDOByIdByFeign(String stockLogId);

    @PostMapping("/updateStockLogDOByFeign")
    int updateStockLogDOByFeign(StockLogDO record);

    @PostMapping("/increaseSalesByFeign")
    void increaseSalesByFeign(@RequestParam("itemId") Integer itemId,@RequestParam("amount") Integer amount);
}
