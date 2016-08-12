package org.fastframework.mvc;

import org.fastframework.mvc.interceptor.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author tonyhui
 * @since 16/8/12
 */
public class HandlerExecutionChain {
    private Object handler;
    private List<HandlerInterceptor> interceptorList;
    private int interceptorIndex = -1;

    public HandlerExecutionChain(Object handler, List<HandlerInterceptor> interceptors) {
        this.handler = handler;
        this.interceptorList = interceptors;
    }

    public Object getHandler() {
        return handler;
    }

    public boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        for (int i = 0; i < interceptorList.size(); i++) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            if (!interceptor.preHandle(request, response, handler)) {
                // TODO: 16/8/12 调用triggerAfterCompletion方法
                return false;
            }
            interceptorIndex = i;
        }
        return true;
    }

    public void applyPostHandle(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
        for (int i = interceptorIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            interceptor.postHandle(request, response, handler, result);
        }
    }
}
