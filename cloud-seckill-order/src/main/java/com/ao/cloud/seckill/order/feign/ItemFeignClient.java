package com.ao.cloud.seckill.order.feign;

import com.ao.cloud.seckill.common.response.ApiRestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "cloud-seckill-item")
public interface ItemFeignClient {

    @PostMapping("/validateByFeign")
    ApiRestResponse<Boolean> validateByFeign(@RequestParam("promoId") Integer promoId, @RequestParam("itemId") Integer itemId);


}
