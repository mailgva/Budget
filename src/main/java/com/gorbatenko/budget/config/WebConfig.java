package com.gorbatenko.budget.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/webjars/**",
                             "/images/**",
                             "/css/**"   )
                .addResourceLocations(
                        "/webjars/",
                        "classpath:/static/images/")
                .addResourceLocations(
                        "/webjars/",
                        "classpath:/static/css/");

    }

}