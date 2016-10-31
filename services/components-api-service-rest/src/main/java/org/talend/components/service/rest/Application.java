package org.talend.components.service.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.talend.daikon.spring.BndToSpringBeanNameGenerator;

@SpringBootApplication
@ComponentScan(basePackages = {"org.talend.components", "org.talend.daikon"}, nameGenerator = BndToSpringBeanNameGenerator.class, includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = aQute.bnd.annotation.component.Component.class) , excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Osgi") )
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
