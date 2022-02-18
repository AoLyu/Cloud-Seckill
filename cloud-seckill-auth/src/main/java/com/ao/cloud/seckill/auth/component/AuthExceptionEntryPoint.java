package com.ao.cloud.seckill.auth.component;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthExceptionEntryPoint implements AuthenticationEntryPoint
{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws ServletException {
        Map<String, Object> map = new HashMap<String, Object>();
        Throwable cause = authException.getCause();

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=utf-8");;
        try {
            if(cause instanceof InvalidTokenException) {
                response.getWriter().write(
                        " {   \"status\": 10009,\n" +
                                "    \"msg\": \"凭证无效或已过期\",\n" +
                                "    \"data\": null}"
                );
            }else{
                response.getWriter().write(
                        "    \"status\": 10009,\n" +
                        "    \"msg\": \"认证失败\",\n" +
                        "    \"data\": null"
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
