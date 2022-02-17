package com.ao.cloud.seckill.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisConfig {

//    @Autowired
//    private RedisConnectionFactory redisConnectionFactory;

    // 使用Redis存储Token，不然Token存在内存中
//    @Bean
//    public TokenStore redisTokenStore(){
//        return new RedisTokenStore(redisConnectionFactory);
//    }
}
