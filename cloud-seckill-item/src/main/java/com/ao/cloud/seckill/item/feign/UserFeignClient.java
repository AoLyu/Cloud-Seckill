package com.ao.cloud.seckill.item.feign;

import com.ao.cloud.seckill.user.model.pojo.UserModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "cloud-seckill-user")
public interface UserFeignClient {

    @GetMapping("/getUserByIdInCacheByFeign")
    UserModel getUserByIdInCacheByFeign(Integer id);
}
