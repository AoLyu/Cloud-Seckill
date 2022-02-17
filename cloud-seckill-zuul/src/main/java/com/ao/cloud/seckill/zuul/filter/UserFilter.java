package com.ao.cloud.seckill.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Component
public class UserFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String requestURI = request.getRequestURI();
        if(requestURI.contains("order"))
            return true;
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        currentContext.getResponse().setContentType("application/json;charset=utf-8");
        String header = request.getHeader("Authorization");
        if(StringUtils.isBlank(header)) {
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseBody("{\n" +
                    "    \"status\": 10006,\n" +
                    "    \"msg\": \"请先登录\",\n" +
                    "    \"data\": null\n" +
                    "}");
        } else {
            String token = header.substring(header.indexOf("Bearer") + 7);

            Claims body = Jwts.parser()
                    .setSigningKey("test_key".getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();

            String userName = (String) body.get("user_name");
            String userId =  body.get("user_id").toString();
            currentContext.addZuulRequestHeader("user_id",userId);
            currentContext.addZuulRequestHeader("user_Name",userName);
        }

        currentContext.setResponseStatusCode(200);
        return null;
    }
}
