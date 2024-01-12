package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginRequiredInterceptor;
import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import com.nowcoder.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对所有静态资源不拦截，对所有动态资源都拦截
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.img", "/**/*.html", "/**/*.jpeg");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.img", "/**/*.html", "/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.img", "/**/*.html", "/**/*.jpeg");
    }
}
