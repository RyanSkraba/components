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
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

import static org.talend.components.api.schema.SchemaFactory.newSchemaElement;

public class OauthProperties extends ComponentProperties {

    public SchemaElement clientId = newSchemaElement("clientId", "Client Id").setRequired(true);

    public SchemaElement clientSecret = newSchemaElement("clientSecret", "Client Secret").setRequired(true);

    public SchemaElement callbackHost = newSchemaElement("callbackHost", "Callback Host").setRequired(true);

    public SchemaElement callbackPort = newSchemaElement(SchemaElement.Type.INT, "callbackPort", "Callback Port").setRequired(
            true);

    public SchemaElement tokenFile = newSchemaElement("tokenFile", "Token File").setRequired(true);

    public static final String OAUTH = "OAuth";

    public OauthProperties() {
        setupLayout();
    }

    @Override
    protected void setupLayout() {
        super.setupLayout();

        Form form = Form.create(this, OAUTH, "OAuth Parameters");
        form.addRow(clientId);
        form.addColumn(clientSecret);

        form.addRow(callbackHost);
        form.addColumn(callbackPort);

        form.addRow(tokenFile);
    }
}
