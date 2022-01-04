package com.ao.cloud.seckill.zuul.service;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface AuthorityService {
    boolean hasPermission(HttpServletRequest request, Authentication authentication);
}
