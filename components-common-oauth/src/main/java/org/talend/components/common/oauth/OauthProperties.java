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

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

public class OauthProperties extends ComponentProperties {

    public SchemaElement clientId = newProperty("clientId").setRequired(true); //$NON-NLS-1$

    public SchemaElement clientSecret = newProperty("clientSecret").setRequired(true); //$NON-NLS-1$

    public SchemaElement callbackHost = newProperty("callbackHost").setRequired(true); //$NON-NLS-1$

    public SchemaElement callbackPort = newProperty(SchemaElement.Type.INT, "callbackPort").setRequired(true); //$NON-NLS-1$

    public SchemaElement tokenFile = newProperty("tokenFile").setRequired(true); //$NON-NLS-1$

    @Override
    protected void setupLayout() {
        super.setupLayout();

        Form form = Form.create(this, Form.MAIN, "OAuth Parameters");
        form.addRow(clientId);
        form.addColumn(clientSecret);
        form.addRow(callbackHost);
        form.addColumn(callbackPort);
        form.addRow(tokenFile);
    }
}
