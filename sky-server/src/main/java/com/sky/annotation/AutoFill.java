package com.sky.annotation;

import com.sky.enumeration.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.*;
/**
 * 自定义注解，用于标记需要自动填充的字段
 */
@Target(ElementType.METHOD) //注解作用于方法
@Retention(RetentionPolicy.RUNTIME) //注解在运行时有效
public @interface AutoFill {
    //数据库操作类型：UPDATE，INSERT
    OperationType value();
    
}
