package com.ao.cloud.seckill.auth.controller;

import com.ao.cloud.seckill.auth.model.pojo.Oauth2TokenDto;
import com.ao.cloud.seckill.auth.model.pojo.UserModel;
import com.ao.cloud.seckill.auth.model.vo.UserVO;
import com.ao.cloud.seckill.auth.service.UserService;
import com.ao.cloud.seckill.common.exception.CloudSeckillExceptionEnum;
import com.ao.cloud.seckill.common.exception.CloudSekillException;
import com.ao.cloud.seckill.common.response.ApiRestResponse;
import com.ao.cloud.seckill.common.util.MD5Utils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RestController
public class UserController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    //用户注册接口
    @PostMapping(value = "/register")
    public ApiRestResponse register(@RequestParam(name="telephone")String telephone,
                                    @RequestParam(name="otpCode")String otpCode,
                                    @RequestParam(name="name")String name,
                                    @RequestParam(name="gender")Integer gender,
                                    @RequestParam(name="age")Integer age,
                                    @RequestParam(name="password")String password) throws CloudSekillException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的otpcode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if(!StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR);
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telephone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(MD5Utils.getMDStr(password));
        userService.register(userModel);
        return ApiRestResponse.success(null);
    }

    //用户获取otp短信接口
    @GetMapping(value = "/getotp")
    public ApiRestResponse getOtp(@RequestParam(name="telephone")String telephone){
        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt =  random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定他的手机号与OTPCODE
        httpServletRequest.getSession().setAttribute(telephone,otpCode);

        //将OTP验证码通过短信通道发送给用户,省略
        System.out.println("telephone = " + telephone + " & otpCode = "+otpCode);

        return ApiRestResponse.success(null);
    }

    @GetMapping("/get")
    public ApiRestResponse getUser(@RequestParam(name="id") Integer id) throws CloudSekillException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的对应用户信息不存在
        if(userModel == null){
            throw new CloudSekillException(CloudSeckillExceptionEnum.USER_NOT_EXIST);
        }

        //讲核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO  = convertFromModel(userModel);

        //返回通用对象
        return ApiRestResponse.success(userVO);
    }

    private UserVO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

    //用户登陆接口
    @PostMapping(value = "/login")
    public ApiRestResponse login(@RequestParam(name="telephone")String telephone,
                                  @RequestParam(name="password")String password) throws CloudSekillException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(StringUtils.isBlank(telephone)||
                StringUtils.isBlank(password)){
            throw new CloudSekillException(CloudSeckillExceptionEnum.PARAMETER_VALIDATION_ERROR);
        }
        //用户登陆服务,用来校验用户登陆是否合法
        UserModel userModel = userService.validateLogin(telephone,MD5Utils.getMDStr(password));

        //生成登录凭证token，UUID
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-","");
        //建议token和用户登陆态之间的联系
        redisTemplate.opsForValue().set(uuidToken,userModel);
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);

        //下发了token
        return ApiRestResponse.success(uuidToken);
    }

    @Autowired
    private TokenEndpoint tokenEndpoint;

    /**
     * Oauth2登录认证
     */
    @PostMapping(value = "/oauth/token")
    public ApiRestResponse postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2TokenDto oauth2TokenDto = Oauth2TokenDto.builder()
                .token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn())
                .tokenHead("Bearer ").build();
        return ApiRestResponse.success(oauth2TokenDto);
    }

    //供Feign调用
    @GetMapping("/getUserByIdInCacheByFeign")
    public UserModel getUserByIdInCacheByFeign(Integer id) {
        return userService.getUserByIdInCache(id);
    }

}
