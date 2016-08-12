package org.fastframework.mvc;

import org.fastframework.core.Config;
import org.fastframework.core.util.ClassUtil;
import org.fastframework.mvc.interceptor.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tonyhui
 * @since 16/8/12
 */
public class InterceptorCollection {

    /**
     * 保存所有的拦截器
     *
     */
    private static List<HandlerInterceptor> interceptors = new ArrayList<>();

    /**
     * Interceptor扫描包的目录
     */
    private static final String scanPackage = Config.getScanPackage();

    /**
     * 初始化拦截器
     */
    public static void init() {
        // 获取到所有实现了 org.fastframework.mvc.interceptor.HandlerInterceptor 接口的拦截器列表
        List<Class<?>> interceptorClassList = ClassUtil.getClassListByInterface(scanPackage, HandlerInterceptor.class);
        try {
            for (Class<?> interceptor: interceptorClassList) {
                HandlerInterceptor handlerInterceptor = (HandlerInterceptor) interceptor.newInstance();
                interceptors.add(handlerInterceptor);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("初始化拦截器错误, error:" + e);
        }
    }

    public static List<HandlerInterceptor> getInterceptors() {
        return interceptors;
    }
}
