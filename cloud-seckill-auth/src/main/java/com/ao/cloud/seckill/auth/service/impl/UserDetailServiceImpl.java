package com.ao.cloud.seckill.auth.service.impl;

import com.ao.cloud.seckill.auth.model.dataobject.User;
import com.ao.cloud.seckill.auth.model.pojo.UserModel;
import com.ao.cloud.seckill.auth.service.UserService;
import com.ao.cloud.seckill.common.exception.CloudSeckillExceptionEnum;
import com.ao.cloud.seckill.common.exception.CloudSekillException;
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
        UserModel userModel = userService.getUserByName(username);

        if(userModel==null)
            throw new CloudSekillException(CloudSeckillExceptionEnum.PASSWORD_ERROR);
//        String password = userModel.getEncrptPassword();
        String password = passwordEncoder.encode(userModel.getEncrptPassword());
        return new User(username, password, AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));

//        return null;
    }
}
