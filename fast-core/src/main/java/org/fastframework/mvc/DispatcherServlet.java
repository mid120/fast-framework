package org.fastframework.mvc;

import org.fastframework.mvc.util.MVCHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MVC 前端控制器
 *      1. 初始化相关配置: 类扫描/路由匹配
 *      2. 转发请求到 HandlerMapping
 *      3. 反射调用Controller方法,并解耦
 *      4. 根据返回值,响应视图或数据
 *
 * Created by bysocket on 16/7/19.
 */
@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServlet.class);

    /**
     * 初始化相关配置(在web服务启动加载当前servlet的时候会被调用)
     *      扫描类 - 路由Map
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        ControllerCollection.init();
        InterceptorCollection.init();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 设置请求默认编码
            request.setCharacterEncoding(MVCHelper.REQ_CHARACTER_UTF_8);
            // 请求相关信息
            // 请求方法 [POST] [GET]
            String requestMethod = request.getMethod();
            // 请求路由
            String requestPath = MVCHelper.getRequestPath(request);

            LOGGER.debug("[fast framework] {} : {}", requestMethod, requestPath);

            // "/" 请求重定向到默认首页
            if (MVCHelper.URL_PATH_SEPARATOR.equals(requestPath)) {
                MVCHelper.redirectRequest(MVCHelper.REQ_DEFAULT_HOME_PAGE, request, response);
                return;
            }

            // 处理器映射
            // 获取 handler
            HandlerExecutionChain mapperHandler = HandlerMapping.getHandler(requestMethod, requestPath);

            // null == handler
            if (null == mapperHandler) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 调用拦截器的前置方法
            if(!mapperHandler.applyPreHandle(request, response)) {
                return ;
            }

            // 调用 Handler
            Object result = HandlerInvoker.invokeHandler(request, response, mapperHandler.getHandler());

            // 调用拦截器的后置方法
            mapperHandler.applyPostHandle(request, response, result);

            // View 处理
            ViewResolver.resolveView(request,response,result,mapperHandler.getHandler());
        }catch (Exception e) {
            throw new RuntimeException("初始化fast-framwork出错, error:" + e);
        }
    }
}
