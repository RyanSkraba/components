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

public class Oauth2ImplicitFlowProperties extends ComponentPropertiesImpl {

    private static final long serialVersionUID = -769837076726950637L;

    public Property<String> clientId = newProperty("clientId").setRequired(); //$NON-NLS-1$

    public Property<String> clientSecret = newProperty("clientSecret").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> callbackHost = newProperty("callbackHost").setRequired(); //$NON-NLS-1$

    public Property<Integer> callbackPort = newInteger("callbackPort").setRequired(); //$NON-NLS-1$

    public Property<String> tokenFile = newProperty("tokenFile").setRequired(); //$NON-NLS-1$

    public Oauth2ImplicitFlowProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(clientId);
        form.addColumn(clientSecret);
        form.addRow(callbackHost);
        form.addColumn(callbackPort);
        form.addRow(widget(tokenFile).setWidgetType(Widget.FILE_WIDGET_TYPE));
    }
}