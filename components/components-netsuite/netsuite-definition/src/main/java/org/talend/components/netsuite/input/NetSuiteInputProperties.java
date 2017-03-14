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

package org.talend.components.netsuite.input;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.netsuite.NetSuiteProvideConnectionProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.properties.presentation.Form;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class NetSuiteInputProperties extends FixedConnectorsComponentProperties
        implements NetSuiteProvideConnectionProperties {

    public final NetSuiteConnectionProperties connection;

    public final NetSuiteInputModuleProperties module;

    protected transient PropertyPathConnector MAIN_CONNECTOR =
            new PropertyPathConnector(Connector.MAIN_NAME, "module.main");

    public NetSuiteInputProperties(@JsonProperty("name") String name) {
        super(name);

        connection = new NetSuiteConnectionProperties("connection");

        module = new NetSuiteInputModuleProperties("module", connection);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(module.getForm(Form.REFERENCE));
    }

    @Override
    public NetSuiteConnectionProperties getConnectionProperties() {
        return connection.getConnectionProperties();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        }
        return Collections.emptySet();
    }
}
