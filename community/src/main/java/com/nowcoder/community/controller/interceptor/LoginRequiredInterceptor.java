package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // HandlerMethod 是SpringMVC提供的一个类型，如果拦截到的是一个方法，则会是 HandlerMethod 的类型
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod)handler;
            // 尝试从拦截到的方法中去取指定的注解
            LoginRequired loginAnnotation = method.getMethodAnnotation(LoginRequired.class);
            if (loginAnnotation != null && hostHolder.getUser() == null) {
                // 该方法是接口声明的，不能用 return去重定向，那么就用 response 去重定向
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }

        }
        return true;
    }
}
