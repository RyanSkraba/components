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
package org.talend.components.localio.rowgenerator;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class RowGeneratorProperties extends FixedConnectorsComponentProperties {

    public RowGeneratorProperties(String name) {
        super(name);
    }

    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public Property<Boolean> useSeed = PropertyFactory.newBoolean("useSeed", false);

    public Property<Long> seed = PropertyFactory.newProperty(Long.class, "seed").setValue(0L);

    public Property<Long> nbRows = PropertyFactory.newProperty(Long.class, "nbRows").setValue(100L);

    public Property<Integer> nbPartitions = PropertyFactory.newInteger("nbPartitions", 1);

    // TODO(rskraba): implement
    public Property<Boolean> usePartitionSkew = PropertyFactory.newBoolean("usePartitionSkew", false);

    // TODO(rskraba): implement
    public Property<Long> nbRowsMaxSkewed = PropertyFactory.newProperty(Long.class, "nbRowsMaxSkewed").setValue(0L);

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(schemaFlow.getForm(Form.REFERENCE));
        mainForm.addRow(useSeed);
        mainForm.addRow(seed);
        mainForm.addRow(nbRows);
        mainForm.addRow(nbPartitions);
        mainForm.addRow(usePartitionSkew);
        mainForm.addRow(nbRowsMaxSkewed);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(seed).setVisible(useSeed);
            form.getWidget(nbRowsMaxSkewed).setVisible(usePartitionSkew);
        }
    }

    public void afterUseSeed() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUsePartitionSkew() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            // output schema
            connectors.add(FLOW_CONNECTOR);
        }
        return connectors;
    }
}
