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
package org.talend.components.jira;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link JiraConnectionProperties} class
 */
public class JiraConnectionPropertiesTest {

    /**
     * Checks {@link JiraConnectionProperties#setupProperties()} sets correct initial property values
     */
    @Test
    public void testSetupProperties() {
        JiraConnectionProperties properties = new JiraConnectionProperties("root");
        properties.setupProperties();

        String hostUrlValue = properties.hostUrl.getValue();
        assertThat(hostUrlValue, equalTo("https://jira.atlassian.com"));
    }

    /**
     * Checks {@link JiraConnectionProperties#setupLayout()} creates Main form, which contain 3 widgets and checks
     * widgets names <br>
     */
    @Test
    public void testSetupLayout() {
        JiraConnectionProperties properties = new JiraConnectionProperties("root");
        properties.basicAuthentication.init();

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(2));
        Widget hostUrlWidget = main.getWidget("hostUrl");
        assertThat(hostUrlWidget, notNullValue());
        Widget basicAuthenticationWidget = main.getWidget("basicAuthentication");
        assertThat(basicAuthenticationWidget, notNullValue());
    }
}
