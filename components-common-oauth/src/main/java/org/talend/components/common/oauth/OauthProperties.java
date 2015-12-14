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

import static org.talend.components.api.properties.PropertyFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;

public class OauthProperties extends ComponentProperties {

    public Property clientId = (Property) newProperty("clientId").setRequired(true); //$NON-NLS-1$

    public Property clientSecret = (Property) newProperty("clientSecret").setRequired(true); //$NON-NLS-1$

    public Property callbackHost = (Property) newProperty("callbackHost").setRequired(true); //$NON-NLS-1$

    public Property callbackPort = (Property) newProperty(Type.INT, "callbackPort").setRequired(true); //$NON-NLS-1$

    public Property tokenFile = (Property) newProperty("tokenFile").setRequired(true); //$NON-NLS-1$

    public OauthProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "OAuth Parameters");
        form.addRow(clientId);
        form.addColumn(clientSecret);
        form.addRow(callbackHost);
        form.addColumn(callbackPort);
        form.addRow(tokenFile);
    }
}
