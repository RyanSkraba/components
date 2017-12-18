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

package ${package}.definition.output;

import ${packageTalend}.api.component.Connector;
import ${packageTalend}.api.component.PropertyPathConnector;
import ${packageTalend}.common.FixedConnectorsComponentProperties;
import ${packageTalend}.common.dataset.DatasetProperties;
import ${packageTalend}.common.io.IOProperties;
import ${packageTalend}.${componentNameLowerCase}.definition.${componentNameClass}DatasetDefinition;
import ${packageTalend}.${componentNameLowerCase}.definition.${componentNameClass}DatasetProperties;
import ${packageDaikon}.properties.ReferenceProperties;
import ${packageDaikon}.properties.presentation.Form;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ${componentNameClass}OutputProperties extends FixedConnectorsComponentProperties implements IOProperties {

    public ReferenceProperties<${componentNameClass}DatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            ${componentNameClass}DatasetDefinition.NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public ${componentNameClass}OutputProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
         super.setupLayout();
         Form mainForm = new Form(this, Form.MAIN);
    }

    @Override
    public ${componentNameClass}DatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            return Collections.EMPTY_SET;
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }
}
