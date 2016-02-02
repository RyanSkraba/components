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
package org.talend.components.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class AbstractComponentTest {

    @Rule
    public TestName name = new TestName();

    long startTime;

    @Before
    public void before() throws Exception {
        startTime = System.currentTimeMillis();
        System.out.println(">>>>> " + name.getMethodName());
    }

    @After
    public void after() throws Exception {
        System.out.println("<<<<< " + name.getMethodName() + " time: " + (System.currentTimeMillis() - startTime));
    }

    /**
     * Getter for componentService.
     * 
     * @return the componentService
     */
    abstract public ComponentService getComponentService();

}
