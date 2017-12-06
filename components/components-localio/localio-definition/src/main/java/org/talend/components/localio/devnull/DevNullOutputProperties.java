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
package org.talend.components.localio.devnull;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.localio.fixed.FixedDatasetDefinition;
import org.talend.components.localio.fixed.FixedDatasetProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class DevNullOutputProperties extends FixedConnectorsComponentProperties implements IOProperties<DatasetProperties> {

    public DevNullOutputProperties(String name) {
        super(name);
    }

    public transient PropertyPathConnector IN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "incoming");

    public SchemaProperties incoming = new SchemaProperties("incoming");

    public transient ReferenceProperties<FixedDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            FixedDatasetDefinition.NAME);

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
    }

    @Override
    public DatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (!isOutputConnection) {
            // output schema
            connectors.add(IN_CONNECTOR);
        }
        return connectors;
    }

}
