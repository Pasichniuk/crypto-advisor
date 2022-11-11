package com.xm.crypto.config;

import com.xm.crypto.filter.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Additional Spring MVC configuration
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor interceptor;

    /**
     * Adds the provided RateLimitInterceptor to the interceptor registry
     * @param registry interceptorRegistry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}