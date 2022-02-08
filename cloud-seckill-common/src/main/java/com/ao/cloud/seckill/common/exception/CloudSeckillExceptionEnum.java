package com.ao.cloud.seckill.common.exception;


/**
 * 异常枚举
 */
public enum CloudSeckillExceptionEnum {

    REQUEST_PARAM_ERROR(10001,"参数有误，请重试"),
    USER_NOT_EXIST(10002,"用户不存在"),
    USER_LOGIN_FAIL(10003,"用户登录失败"),
    PARAMETER_VALIDATION_ERROR(10004,"短信验证码错误"),
    UNKNOWN_ERROR(10005,"未知错误"),
    USER_NOT_LOGIN(10006,"用户未登录"),
    STOCK_NOT_ENOUGH(10007,"库存不够"),
    RATELIMIT(10008,"限流中"),
    AUTHENTICATION_ERROR(10009,"认证错误"),
    GATEWAY_ERROR(10010,"网关错误"),
    REQUEST_ERROR(10011,"请求错误"),
    SYSTEM_ERROR(20000,"系统异常");


    /**
     * 异常码
     */
    Integer code;

    /**
     * 异常信息
     */
    String msg;

    CloudSeckillExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }


}
