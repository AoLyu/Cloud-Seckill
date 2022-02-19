package com.ao.cloud.seckill.order.feign;

import com.ao.cloud.seckill.common.response.ApiRestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(value = "cloud-seckill-item")
public interface ItemFeignClient {

    @PostMapping("/validateByFeign")
    ApiRestResponse<Boolean> validateByFeign(@RequestParam("itemId") Integer itemId,@RequestParam("promoId") Integer promoId);

    @PostMapping("/getItemPriceByFeign")
    ApiRestResponse<BigDecimal> getCurrentPriceByItemIdByFeign(@RequestParam("itemId") Integer itemId,@RequestParam("promoId") Integer promoId);
}
