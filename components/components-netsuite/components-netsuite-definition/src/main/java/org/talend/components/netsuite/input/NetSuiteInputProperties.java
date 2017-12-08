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

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.netsuite.NetSuiteProvideConnectionProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root properties of NetSuite Input component.
 */
public class NetSuiteInputProperties extends FixedConnectorsComponentProperties
        implements NetSuiteProvideConnectionProperties {

    public final NetSuiteConnectionProperties connection;

    public final NetSuiteInputModuleProperties module;

    public final Property<Boolean> bodyFieldsOnly = newBoolean("bodyFieldsOnly", true);

    protected transient final PropertyPathConnector mainConnector =
            new PropertyPathConnector(Connector.MAIN_NAME, "module.main");

    public NetSuiteInputProperties(@JsonProperty("name") String name) {
        super(name);

        connection = new NetSuiteConnectionProperties("connection");
        module = new NetSuiteInputModuleProperties("module", connection);
        bodyFieldsOnly.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(module.getForm(Form.REFERENCE));

        Form advForm = Form.create(this, Form.ADVANCED);
        advForm.addRow(bodyFieldsOnly);
        advForm.addRow(module.getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

    @Override
    public NetSuiteConnectionProperties getConnectionProperties() {
        return connection.getEffectiveConnectionProperties();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(mainConnector);
        }
        return Collections.emptySet();
    }
}
