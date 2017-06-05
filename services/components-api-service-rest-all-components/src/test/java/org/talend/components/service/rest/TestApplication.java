// ============================================================================
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
// ============================================================================
package org.talend.components.service.rest;

import org.springframework.boot.SpringApplication;
import org.talend.components.service.spring.SpringTestApp;

/**
 * this only serves the purpose of starting a debugging session from an IDE easily.
 */
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApp.class, args);
    }
}
