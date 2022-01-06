package com.ao.cloud.seckill.auth.component;

import cn.hutool.json.JSONUtil;
import com.ao.cloud.seckill.common.exception.CloudSeckillExceptionEnum;
import com.ao.cloud.seckill.common.response.ApiRestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("customAccessDeniedHandler")
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");

        try {
            response.getWriter().write(
                    "    \"status\": 10009,\n" +
                    "    \"msg\": \"拒绝访问\",\n" +
                    "    \"data\": null");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
