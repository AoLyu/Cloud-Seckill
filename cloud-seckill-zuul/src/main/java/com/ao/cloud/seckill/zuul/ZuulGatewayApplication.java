package com.ao.cloud.seckill.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableZuulProxy
@SpringBootApplication
@EnableRedisHttpSession
public class ZuulGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayApplication.class,args);
    }
}
