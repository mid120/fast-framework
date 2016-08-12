package org.fastframework.mvc;

import org.fastframework.mvc.bean.HandlerBody;
import org.fastframework.util.ReflectUtil;
import org.fastframework.util.WebUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Handler 调用器
 *
 * Created by bysocket on 16/8/9.
 */
public class HandlerInvoker {

	public static Object invokeHandler(HttpServletRequest request, HttpServletResponse response, Object handler) {

		if(!(handler instanceof HandlerBody)) throw new RuntimeException("不能存在匹配的处理器");
		HandlerBody handlerBody = (HandlerBody) handler;

		// 从 Request 获取参数 - Controller.Method 的 ParamList
		List<Object> controllerMethodParamList = WebUtil.getRequestParamMap(request);

		// ReflectUtil 获取 Controller.Method 的返回值

		return ReflectUtil.invokeControllerMethod(handlerBody.getControllerClass(),
												  handlerBody.getControllerMethod(),
												  controllerMethodParamList);
	}

}
