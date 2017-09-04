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
package org.talend.components.common.oauth.properties;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.EnumSet;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class Oauth2JwtFlowProperties extends ComponentPropertiesImpl {

    private static final long serialVersionUID = 5025673639898114506L;

    // ## JWT
    public Property<String> issuer = newProperty("issuer").setRequired(); //$NON-NLS-1$

    public Property<String> subject = newProperty("subject").setRequired(); //$NON-NLS-1$

    // in seconds
    public Property<Integer> expirationTime = newInteger("expirationTime").setRequired(); //$NON-NLS-1$

    // # X509 certificate
    public Property<String> keyStore = newProperty("keyStore").setRequired(); //$NON-NLS-1$

    public Property<String> keyStorePassword = newProperty("keyStorePassword").setRequired() //$NON-NLS-1$
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> certificateAlias = newProperty("certificateAlias").setRequired(); //$NON-NLS-1$

    public Oauth2JwtFlowProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);

        // JWT properties
        mainForm.addRow(issuer);
        mainForm.addRow(subject);
        mainForm.addRow(expirationTime);

        // X509 certificate properties
        mainForm.addRow(widget(keyStore).setWidgetType(Widget.FILE_WIDGET_TYPE));
        mainForm.addRow(widget(keyStorePassword).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addColumn(certificateAlias);
    }
}
