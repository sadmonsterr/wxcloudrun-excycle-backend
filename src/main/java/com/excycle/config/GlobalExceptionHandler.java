package com.excycle.config;

import com.excycle.common.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e)
    {
        e.printStackTrace();
//        return "error";
        return Result.error("服务器异常," + e.getMessage());
    }
}
