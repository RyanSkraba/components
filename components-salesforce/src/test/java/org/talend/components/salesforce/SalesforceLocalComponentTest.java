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
package org.talend.components.salesforce;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.ComponentTestUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

public class SalesforceLocalComponentTest {

    ComponentService componentService = new ComponentServiceImpl(null);

    @BeforeClass public static void init() {
        ComponentTestUtils.setupGlobalContext();
    }

    @AfterClass public static void unset() {
        ComponentTestUtils.unsetGlobalContext();
    }

    public SalesforceLocalComponentTest() {
    }

    protected ComponentProperties checkAndBefore(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallBefore());
        return componentService.beforeProperty(propName, props);
    }

    protected ComponentProperties checkAndAfter(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallAfter());
        return componentService.afterProperty(propName, props);
    }

    protected ComponentProperties checkAndValidate(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallValidate());
        return componentService.validateProperty(propName, props);
    }

    @Test public void testGetProps() {
        ComponentProperties props = new TSalesforceConnectionDefinition().createProperties();
        Form f = props.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(props);
        System.out.println(f);
        System.out.println(props);
        assertEquals(Form.MAIN, f.getName());
    }

    @Test public void testAfterLoginType() throws Throwable {
        ComponentProperties props;

        props = new TSalesforceConnectionDefinition().createProperties();
        ComponentTestUtils.checkSerialize(props);
        SchemaElement loginType = props.getProperty("loginType");
        System.out.println(loginType.getPossibleValues());
        assertEquals("Basic", loginType.getPossibleValues().get(0).toString());
        assertEquals("OAuth", loginType.getPossibleValues().get(1).toString());
        assertEquals(SalesforceConnectionProperties.LOGIN_BASIC, props.getValue(loginType));
        Form mainForm = props.getForm(Form.MAIN);
        assertEquals("Salesforce Connection Settings", mainForm.getTitle());
        assertTrue(mainForm.getWidget(SalesforceUserPasswordProperties.class).isVisible());
        assertFalse(mainForm.getWidget(OauthProperties.class).isVisible());

        props.setValue(loginType, SalesforceConnectionProperties.LOGIN_OAUTH);
        props = checkAndAfter(mainForm, "loginType", props);
        mainForm = props.getForm(Form.MAIN);
        assertTrue(mainForm.isRefreshUI());

        assertFalse(mainForm.getWidget(SalesforceUserPasswordProperties.class).isVisible());
        assertTrue(mainForm.getWidget(OauthProperties.class).isVisible());
    }

    @Test public void testInputProps() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputDefinition().createProperties();
        assertEquals(2, props.queryMode.getPossibleValues().size());

        SchemaElement returns = props.getProperty(ComponentProperties.RETURNS);
        assertEquals("NB_LINE", returns.getChildren().get(0).getName());
    }
}