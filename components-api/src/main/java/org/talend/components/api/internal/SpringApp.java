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
package org.talend.components.api.internal;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring application to launch the component service
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.talend.components")
public class SpringApp implements DisposableBean {

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }

    public void destroy() throws Exception {
        // nothing to be done yet.
    }

}
