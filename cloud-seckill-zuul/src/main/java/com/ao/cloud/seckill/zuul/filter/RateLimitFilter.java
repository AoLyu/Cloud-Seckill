package com.ao.cloud.seckill.zuul.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVLET_DETECTION_FILTER_ORDER;

@Component
public class RateLimitFilter extends ZuulFilter {
    //限流桶实现 每秒钟1000个令牌
    private static final RateLimiter RATELIMITER=RateLimiter.create(1000);

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        //限流过滤器要最先运行
        return SERVLET_DETECTION_FILTER_ORDER-1;
    }

    @Override
    public boolean shouldFilter() {

        return false;  // 测试不拦截
//        RequestContext requestContext = RequestContext.getCurrentContext();
//        HttpServletRequest request = requestContext.getRequest();
//
//        //只对订单接口限流，进行拦截，就会进入下面的 run方法中
//        if (request.getRequestURI().contains("/createorder")){
//            return true;
//        }
//        //不拦截，放行
//        return false;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        if(!RATELIMITER.tryAcquire()){//判断是否取到令牌
            //没有取到令牌 直接抛出异常
//            throw new RateLimterException();
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseBody("{\n" +
                    "    \"status\": 10006,\n" +
                    "    \"msg\": \"NEED_LOGIN\",\n" +
                    "    \"data\": null\n" +
                    "}");
        }
        currentContext.setResponseStatusCode(200);
        return null;
    }
}
