package com.ydl.sms.aspect;

import com.ydl.context.BaseContextHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 通过切面方式，自定义注解，实现实体基础数据的注入（创建者、创建时间、修改者、修改时间）
 */
@Component // 交给spring
@Aspect   // 开启aop
@Slf4j
public class DefaultParamsAspect {
    /**
     * 在新增和修改的方法上面加上@DefaultParams
     * 那么在新增和修改之前 and  在数据库插之前   把一些属性填到实体类中
     *
     * @param point 连接点
     */
    @SneakyThrows
    @Before("@annotation(com.ydl.sms.annotation.DefaultParams)")
    public void beforeEvent(JoinPoint point) {
        // 从threadlocal本地线程中取得userID
        Long userId = BaseContextHandler.getUserId();

        // 判断参数中有无id,有id则作为修改项,否则作为新增项
        // 通过连接点获取参数实例列表  (A、B....)
        Object[] argsList = point.getArgs();

        // 遍历获取每一个args参数实例
        for (Object args : argsList) {
            // 获取参数实例类型  A --> B
            Class<?> clazz = args.getClass();
            // 调用方法A.getId() 无参
            Method getIdMethod = getMethod(clazz, "getId");
            Object id = null;
            if (getIdMethod != null) {
                id = getIdMethod.invoke(args);
            }

            // 当id为空就表示是这一次时需要插入的数据,添加参数(创建人,创建时间,修改人,修改时间)
            if (id == null) {
                // 新增创建人和时间
                Method setIdMethod = getMethod(clazz, "setCreateUser", String.class);
                if (setIdMethod != null) {
                    setIdMethod.invoke(args, userId.toString());
                }
                Method setCreateTimeMethod = getMethod(clazz, "setCreateTime", LocalDateTime.class);
                if (setCreateTimeMethod != null) {
                    setCreateTimeMethod.invoke(args, LocalDateTime.now());
                }

                // 新增修改人和修改时间
                Method setUpdateUserMethod = getMethod(clazz, "setUpdateUser", String.class);
                if (setUpdateUserMethod != null) {
                    setUpdateUserMethod.invoke(args, userId.toString());
                }
                Method setUpdateTimeMethod = getMethod(clazz, "setUpdateTime", LocalDateTime.class);
                if (setUpdateTimeMethod != null) {
                    setUpdateTimeMethod.invoke(args, LocalDateTime.now());
                }
            }


            // 当id为不空就表示是这一次时需要修改的的数据,添加参数(修改人,修改时间)
            Method setUpdateUserMethod = getMethod(clazz, "setUpdateUser", String.class);
            if (setUpdateUserMethod != null) {
                setUpdateUserMethod.invoke(args, userId.toString());
            }
            Method setUpdateTimeMethod = getMethod(clazz, "setUpdateTime", LocalDateTime.class);
            if (setUpdateTimeMethod != null) {
                setUpdateTimeMethod.invoke(args, LocalDateTime.now());
            }
        }
    }

    /**
     * 获得方法对象
     *
     * @param classes 获取实例类型
     * @param name    方法名
     * @param types   参数类型
     * @return 返回一个方法
     */
    private Method getMethod(Class<?> classes, String name, Class<?>... types) {
        try {
            return classes.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
