package com.sky.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.sky.constant.AutoFillConstant;
import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;

import lombok.extern.slf4j.Slf4j;

import com.sky.context.BaseContext;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现自动填充功能
 */
@Aspect
@Component // 将当前类标记为Spring的组件，使其能够被Spring容器管理
@Slf4j

public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)") // 这条切入点将匹配
                                                                                                 // com.sky.mapper
                                                                                                 // 包下所有类的所有方法
    public void autoFillPointCut() {
    }

    /**
     * 前置通知:进行公共字段的赋值
     */
    @Before("autoFillPointCut()") // 在切入点之前执行
    public void autoFill(JoinPoint joinpoint) {
        log.info("开始进行公告字段自动填充");
        //获取被拦截的方法的签名
        MethodSignature signature = (MethodSignature) joinpoint.getSignature();
        AutoFill autofill = signature.getMethod().getAnnotation(AutoFill.class);
        //获取当前被拦截的方法上的数据库的操作类型
        OperationType operationType = autofill.value();
        //获取被拦截的方法上的参数
        Object[] args = joinpoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        Object object = args[0];
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据数据库的操作类型为对应的属性通过反射进行不同的赋值
        if(operationType == OperationType.INSERT){
            //插入操作，填充创建时间、更新时间、创建人、修改人
            try{
                Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setCreateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);

                setCreateTime.invoke(object, now);
                setCreateUser.invoke(object, currentId);
                setUpdateTime.invoke(object, now);
                setUpdateUser.invoke(object, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            //更新操作，填充更新时间、修改人
            try{
                Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(object, now);
                setUpdateUser.invoke(object, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
            
    }
}
