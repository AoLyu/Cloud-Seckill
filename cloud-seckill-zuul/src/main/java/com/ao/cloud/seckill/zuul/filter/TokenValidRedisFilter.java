package com.ao.cloud.seckill.zuul.filter;

import com.ao.cloud.seckill.zuul.constant.AuthConstants;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class TokenValidRedisFilter extends ZuulFilter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String header = request.getHeader("Authorization");
        if (header.startsWith("Bearer")) {
            String realHead = AuthConstants.REDIS_PREFIX + "auth:" + header.replaceFirst("Bearer ", "");
            String redisHead = stringRedisTemplate.boundValueOps(realHead).get();
            if (redisHead == null) {
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(401);
                ctx.setResponseBody("{\n" +
                        "    \"status\": 10004,\n" +
                        "    \"msg\": \"短信验证码错误\",\n" +
                        "    \"data\": null\n" +
                        "}");
                ctx.getResponse().setContentType("text/html;charset=UTF-8");
            }
        }
        return null;

    }
}
