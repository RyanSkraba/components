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
package org.talend.components.common.oauth;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import java.util.EnumSet;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.*;

public class OauthProperties extends ComponentProperties {

    public Property<String> clientId = newProperty("clientId").setRequired(true); //$NON-NLS-1$

    public Property<String> clientSecret = newProperty("clientSecret").setRequired(true)
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));; //$NON-NLS-1$

    public Property<String> callbackHost = newProperty("callbackHost").setRequired(true); //$NON-NLS-1$

    public Property<Integer> callbackPort = newInteger("callbackPort").setRequired(true); //$NON-NLS-1$

    public Property<String> tokenFile = newProperty("tokenFile").setRequired(true); //$NON-NLS-1$

    public OauthProperties(String name) {
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
