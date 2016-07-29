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
package org.talend.components.datastewardship;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.EnumSet;

import org.apache.commons.codec.binary.Base64;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 * TDS Connection {@link Properties}
 */
public class TdsConnectionProperties extends PropertiesImpl {

    /**
     * URL
     */
    public Property<String> url = newProperty("url").setRequired(); //$NON-NLS-1$

    /**
     * User name
     */
    public Property<String> username = newProperty("username").setRequired(); //$NON-NLS-1$

    /**
     * Password
     */
    public Property<String> password = newProperty("password").setRequired() //$NON-NLS-1$
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    /**
     * Constructor sets properties name
     * 
     * @param name properties name
     */
    public TdsConnectionProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(url);
        mainForm.addRow(username);
        mainForm.addColumn(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

    /**
     * Returns value for Http Authorization header. For example, for "user" userId and "pass" password it returns
     * "Basic dXNlcjpwYXNz"
     * 
     * @return value for Http Authorization header
     */
    public String getAuthorizationValue() {
        String credentials = username.getValue() + ":" + password.getValue(); //$NON-NLS-1$
        String encodedCredentials = base64(credentials);
        String authorizationValue = "Basic " + encodedCredentials; //$NON-NLS-1$
        return authorizationValue;
    }

    /**
     * Encodes specified string to base64 and returns result
     * 
     * @param str string to be encoded
     * @return base64 encoded string
     */
    private String base64(String str) {
        return Base64.encodeBase64String(str.getBytes());
    }
}
