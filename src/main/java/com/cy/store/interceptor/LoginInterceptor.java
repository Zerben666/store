package com.cy.store.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 定义一个拦截器 */
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * pre回车出来
     * 检测全局session对象中是否有uid数据。有则放行，没有重定向到login.html
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器（url + Controller：映射）
     * @return 如果返回值为true表示放行当前的请求，如果返回值为false则表示拦截当前的请求
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 通过HttpServletRequest对象获取session全局对象
        Object obj = request.getSession().getAttribute("uid");
        // 用户未登录，重定向
        if (obj == null){
            response.sendRedirect("/web/login.html");
            // 结束后续的调用
            return false;
        }
        // 请求放行
        return true;
    }
}
