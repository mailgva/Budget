package com.gorbatenko.budget.config;

import com.gorbatenko.budget.BaseEntity;
import com.gorbatenko.budget.config.httpconverter.BudgetToHttpConverter;
import com.gorbatenko.budget.config.httpconverter.CurrencyToHttpConverter;
import com.gorbatenko.budget.config.httpconverter.KindToHttpConverter;
import com.gorbatenko.budget.config.httpconverter.RegularOperationToHttpConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        List<Class<? extends AbstractHttpMessageConverter<? extends BaseEntity>>> classes =
                List.of(BudgetToHttpConverter.class,
                    CurrencyToHttpConverter.class,
                    KindToHttpConverter.class,
                    RegularOperationToHttpConverter.class);
        classes.stream().forEach(clazz -> converters.add(createConverterByClass(clazz)));
    }

    private AbstractHttpMessageConverter createConverterByClass(Class clazz) {
        MediaType mediaType = new MediaType("application", "x-www-form-urlencoded",
                Charset.forName("UTF-8"));
        try {
            AbstractHttpMessageConverter converter = (AbstractHttpMessageConverter) clazz.newInstance();
            converter.setSupportedMediaTypes(Arrays.asList(mediaType));
            return converter;
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/webjars/**",
                "/images/**",
                "/css/**",
                "/js/**")
                .addResourceLocations(
                        "/webjars/",
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/static/images/");

    }

    private ISpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addDialect(new Java8TimeDialect());
        engine.setTemplateResolver(templateResolver);
        return engine;
    }

}