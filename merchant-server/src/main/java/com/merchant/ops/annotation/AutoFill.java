package com.merchant.ops.annotation;

import com.merchant.ops.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义注解，用于标识需要自动填充的字段
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface AutoFill {
    //自动填充 insert update
    OperationType value();
}
