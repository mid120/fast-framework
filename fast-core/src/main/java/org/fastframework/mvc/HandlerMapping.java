package org.fastframework.mvc;

import org.fastframework.mvc.annotation.RequestMethod;
import org.fastframework.mvc.bean.HandlerBody;
import org.fastframework.mvc.bean.RequestBody;
import org.fastframework.mvc.interceptor.HandlerInterceptor;

import java.util.List;
import java.util.Map;

/**
 * 处理器映射
 *
 * Created by bysocket on 16/8/9.
 */
public class HandlerMapping {

	/**
	 * 处理方法体
	 *
	 * @param requestMethod
	 * @param requestPath
	 * @return
	 */
	public static HandlerExecutionChain getHandler(String requestMethod, String requestPath) {
		HandlerBody handler;

		//  Controller Map 请求 -> 方法体 的映射
		Map<RequestBody, HandlerBody> methodMap = ControllerCollection.getMethodMap();
		List<HandlerInterceptor> interceptors = InterceptorCollection.getInterceptors();
		for (Map.Entry<RequestBody, HandlerBody> methodEntry : methodMap.entrySet()) {
			RequestBody req = methodEntry.getKey();
			String reqPath  = req.getRequestPath();
			RequestMethod reqMethod = req.getRequestMethod();
			if (reqPath.equals(requestPath) && reqMethod.name().equalsIgnoreCase(requestMethod)) {
				handler = methodEntry.getValue();
				if (handler != null) {
					return new HandlerExecutionChain(handler, interceptors);
				}
			}
		}
		return null;
	}
}
