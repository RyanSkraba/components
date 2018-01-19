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
package org.talend.components.google.drive;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.properties.presentation.Form;

public abstract class GoogleDriveComponentProperties extends FixedConnectorsComponentProperties
        implements GoogleDriveProvideConnectionProperties {

    public enum AccessMethod {
        Name,
        Id
    }

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaMain");

    public SchemaProperties schemaMain = new SchemaProperties("schemaMain");

    public GoogleDriveConnectionProperties connection = new GoogleDriveConnectionProperties("connection");

    public GoogleDriveComponentProperties(String name) {
        super(name);
    }

    @Override
    public Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        Set<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));

        Form avancedForm = Form.create(this, Form.ADVANCED);
        avancedForm.addRow(connection.getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

    @Override
    public GoogleDriveConnectionProperties getConnectionProperties() {
        return connection.getEffectiveConnectionProperties();
    }
}
