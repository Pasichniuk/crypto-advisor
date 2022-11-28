package com.crypto.advisor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import com.crypto.advisor.filter.RateLimitInterceptor;
import com.crypto.advisor.entity.Crypto;

/**
 * Additional Spring MVC configuration
 */

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

    @Bean
    public ObjectReader cryptoObjectReader() {

        var mapper = new CsvMapper();

        var schema = mapper.schemaFor(Crypto.class)
            .withSkipFirstDataRow(true);

        return mapper.readerFor(Crypto.class)
            .with(schema);
    }
}