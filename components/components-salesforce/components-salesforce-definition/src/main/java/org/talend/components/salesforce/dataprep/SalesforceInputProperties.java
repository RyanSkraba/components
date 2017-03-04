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
package org.talend.components.salesforce.dataprep;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetDefinition;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;

public class SalesforceInputProperties extends FixedConnectorsComponentProperties
        implements IOProperties<SalesforceDatasetProperties> {

    /**
     * 
     */
    private static final long serialVersionUID = 8370922451458863132L;

    public ReferenceProperties<SalesforceDatasetProperties> dataset = new ReferenceProperties<>("dataset", SalesforceDatasetDefinition.NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public SalesforceInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        // unused form, but we need to create one anyway.
        Form mainForm = new Form(this, Form.MAIN);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public SalesforceDatasetProperties getDatasetProperties() {
        return dataset.getReference();
    }

    @Override
    public void setDatasetProperties(SalesforceDatasetProperties datasetProperties) {
        dataset.setReference(datasetProperties);
    }

}
