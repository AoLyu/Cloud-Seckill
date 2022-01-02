package com.ao.cloud.seckill.user.service;


import com.ao.cloud.seckill.user.model.pojo.UserModel;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);

    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);

    @Transactional
    void register(UserModel userModel);

    /*
        telphone:用户注册手机
        password:用户加密后的密码
         */
    UserModel validateLogin(String telphone,String encrptPassword);
}
