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

package org.talend.components.netsuite.output;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import java.util.HashSet;
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
 *
 */
public class NetSuiteOutputProperties extends FixedConnectorsComponentProperties
        implements NetSuiteProvideConnectionProperties {

    public static final int DEFAULT_BATCH_SIZE = 100;

    public final NetSuiteConnectionProperties connection;

    public final NetSuiteOutputModuleProperties module;

    public final Property<Integer> batchSize = newInteger("batchSize");

    public final Property<Boolean> dieOnError = newBoolean("dieOnError");

    protected transient final PropertyPathConnector mainConnector;

    protected transient final PropertyPathConnector flowConnector;

    protected transient final PropertyPathConnector rejectConnector;

    public NetSuiteOutputProperties(@JsonProperty("name") String name) {
        super(name);

        connection = new NetSuiteConnectionProperties("connection");

        module = new NetSuiteOutputModuleProperties("module", connection);

        mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "module.main");
        flowConnector = new PropertyPathConnector(Connector.MAIN_NAME, "module.flowSchema");
        rejectConnector = new PropertyPathConnector(Connector.REJECT_NAME, "module.rejectSchema");
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        batchSize.setValue(NetSuiteOutputProperties.DEFAULT_BATCH_SIZE);
        dieOnError.setValue(Boolean.TRUE);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));
        mainForm.addRow(module.getForm(Form.REFERENCE));
        mainForm.addRow(dieOnError);

        Form advForm = Form.create(this, Form.ADVANCED);
        advForm.addRow(module.getForm(Form.ADVANCED));
        advForm.addRow(batchSize);
    }

    @Override
    public NetSuiteConnectionProperties getConnectionProperties() {
        return connection.getEffectiveConnectionProperties();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        Set<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(flowConnector);
            connectors.add(rejectConnector);
        } else {
            connectors.add(mainConnector);
        }
        return connectors;
    }

}
