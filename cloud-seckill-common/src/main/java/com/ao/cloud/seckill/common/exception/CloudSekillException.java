package com.ao.cloud.seckill.common.exception;

/**
 *  统一异常
 */
public class CloudSekillException extends RuntimeException {
    private  final  Integer code;
    private  final  String msg;

    public CloudSekillException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CloudSekillException(CloudSeckillExceptionEnum exceptionEnum){
        this.msg = exceptionEnum.getMsg();
        this.code = exceptionEnum.getCode();
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
