// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.service.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.talend.daikon.spring.BndToSpringBeanNameGenerator;

/**
 * Main Spring application to launch the component service for tests purposes.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.talend.components", //
nameGenerator = BndToSpringBeanNameGenerator.class, //
includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = aQute.bnd.annotation.component.Component.class) , //
excludeFilters = { //
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Osgi") })
public class SpringTestApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApp.class, args);
    }

}
