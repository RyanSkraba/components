package org.talend.components.service.rest;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.talend.daikon.spring.BndToSpringBeanNameGenerator;

@SpringBootApplication
@ComponentScan(basePackages = "org.talend.components", nameGenerator = BndToSpringBeanNameGenerator.class, includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = aQute.bnd.annotation.component.Component.class) , excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Osgi") )
public class Application extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        converters.add(new JsonSchema2HttpMessageConverter());

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
