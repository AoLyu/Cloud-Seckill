package com.ao.cloud.seckill.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

@Configuration
public class LettuceRedisConfig {

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
        //创建 redisTemplate 模版
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();

        //设置 value 的转化格式和 key 的转化格式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        //关联 redisConnectionFactory
        redisTemplate.setConnectionFactory(connectionFactory);

        return redisTemplate;
    }
}
