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

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;

public abstract class AbstractComponentTestIT {

    @Inject
    public ComponentService componentService;

    public AbstractComponentTestIT() {
    }

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

    protected ComponentProperties checkAndBeforeActivate(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallBeforeActivate());
        return componentService.beforePropertyActivate(propName, props);
    }

    protected ComponentProperties checkAndBeforePresent(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallBeforePresent());
        return componentService.beforePropertyPresent(propName, props);
    }

    protected ComponentProperties checkAndAfter(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallAfter());
        return componentService.afterProperty(propName, props);
    }

    protected ComponentProperties checkAndValidate(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallValidate());
        return componentService.validateProperty(propName, props);
    }

}
