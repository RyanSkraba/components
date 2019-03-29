//==============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================
package org.talend.components.service.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = { "org.talend.components", "org.talend.daikon" },
               excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Osgi"))
public class Application {

    public static void main(String[] args) {
        workAroundJava11();
        SpringApplication.run(Application.class, args);
    }

    private static void workAroundJava11() {
        daikonWorkaround();
    }

    private static void daikonWorkaround() {
        if (System.getProperty("sun.boot.class.path") == null) {
            System.setProperty("sun.boot.class.path", System.getProperty("java.class.path"));
        }
    }
}
