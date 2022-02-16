package com.ao.cloud.seckill.auth.service.impl;

import com.ao.cloud.seckill.auth.model.dataobject.User;
import com.ao.cloud.seckill.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    //用户名改为手机号验证
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //待完成
        //使用userService完成用户登录

        String password = passwordEncoder.encode("123456");
        return new User("admin", password, AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));

//        return null;
    }
}
