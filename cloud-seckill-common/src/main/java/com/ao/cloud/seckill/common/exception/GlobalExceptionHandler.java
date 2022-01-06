package com.ao.cloud.seckill.common.exception;

import com.ao.cloud.seckill.common.response.ApiRestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理统一异常的handler
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public  ApiRestResponse handlerException(Exception e){
        log.error("Default Exception: ", e);
        return ApiRestResponse.error(CloudSeckillExceptionEnum.SYSTEM_ERROR );
    }


    @ExceptionHandler(CloudSekillException.class)
    public  ApiRestResponse handlerCloudSeckillException(CloudSekillException e){
        log.error("CloudSeckillException: ", e.getMsg());
        return ApiRestResponse.error(e.getCode(),e.getMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiRestResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException: " ,e);
        return handleBindingResult(e.getBindingResult());
    }

    private ApiRestResponse handleBindingResult(BindingResult result){
        //把异常处理为对外暴露的提示
        List<String> list = new ArrayList<String>();
        if(result.hasErrors()){
            List<ObjectError> allErrors = result.getAllErrors();
            for(ObjectError objectError: allErrors){
                String message = objectError.getDefaultMessage();
                list.add(message);
            }
        }
        if(list.size()==0){
            return ApiRestResponse.error(CloudSeckillExceptionEnum.REQUEST_PARAM_ERROR);
        }

        return  ApiRestResponse.error(CloudSeckillExceptionEnum.REQUEST_PARAM_ERROR.getCode(),list.toString());
    }

}
