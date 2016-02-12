// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.webtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.daikon.spring.BndToSpringBeanNameGenerator;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used the test the component service and the Salesforce components with an external web service.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.talend.components", nameGenerator = BndToSpringBeanNameGenerator.class, includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = aQute.bnd.annotation.component.Component.class) , excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Osgi"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.talend\\.components\\.webtest.*") })
// @Configuration
// @EnableWebMvc
public class TestApplication {// extends WebMvcConfigurerAdapter {

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        return new Jackson2ObjectMapperBuilder() {

            @Override
            public void configure(ObjectMapper objectMapper) {
                super.configure(objectMapper);
                objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
                objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
