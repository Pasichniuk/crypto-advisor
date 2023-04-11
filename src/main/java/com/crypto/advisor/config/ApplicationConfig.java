package com.crypto.advisor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.crypto.advisor.filter.RateLimitInterceptor;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor interceptor;

    @Autowired
    public ApplicationConfig(RateLimitInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}