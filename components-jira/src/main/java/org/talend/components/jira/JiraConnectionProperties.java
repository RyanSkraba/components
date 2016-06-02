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

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.StringProperty;
import org.talend.daikon.properties.presentation.Form;

/**
 * Jira connection {@link Properties}
 */
public class JiraConnectionProperties extends ComponentProperties {
    
    private static final String INITIAL_HOST = "https://jira.atlassian.com";
    
    /**
     * Host URL
     */
    public StringProperty hostUrl = PropertyFactory.newString("hostUrl");
    
    /**
     * User id and password properties for Basic Authentication
     */
    public BasicAuthenticationProperties basicAuthentication = new BasicAuthenticationProperties("basicAuthentication"); 

    /**
     * Constructor sets properties name
     * 
     * @param name properties name
     */
    public JiraConnectionProperties(String name) {
        super(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        hostUrl.setValue(INITIAL_HOST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(hostUrl);
        mainForm.addRow(basicAuthentication.getForm(Form.MAIN));
    }
}
