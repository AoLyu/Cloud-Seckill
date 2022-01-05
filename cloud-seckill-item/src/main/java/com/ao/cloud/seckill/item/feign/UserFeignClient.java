package com.ao.cloud.seckill.item.feign;

import com.ao.cloud.seckill.auth.model.pojo.UserModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "cloud-seckill-auth")
public interface UserFeignClient {

    @GetMapping("/getUserByIdInCacheByFeign")
    UserModel getUserByIdInCacheByFeign(Integer id);
}
