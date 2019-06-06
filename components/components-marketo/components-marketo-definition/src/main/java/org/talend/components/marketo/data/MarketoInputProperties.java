// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.data;

import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_PARAM;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class MarketoInputProperties extends FixedConnectorsComponentProperties
        implements MarketoProvideConnectionProperties, IOProperties<MarketoDatasetProperties> {

    public ReferenceProperties<MarketoDatasetProperties> dataset = new ReferenceProperties<>("dataset",
            MarketoDatasetDefinition.COMPONENT_NAME);

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main");

    public Property<String> leadKeyType = newString("leadKeyType").setRequired();

    public Property<String> leadKeyValue = newString("leadKeyValue").setRequired();

    public Property<String> sinceDateTime = newString("sinceDateTime");

    public Property<String> fieldList = newString("fieldList");

    public MarketoInputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public MarketoDatasetProperties getDatasetProperties() {
        return dataset.getReference();
    }

    @Override
    public void setDatasetProperties(MarketoDatasetProperties datasetProperties) {
        this.dataset.setReference(datasetProperties);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        leadKeyType.setPossibleValues(Arrays.asList("email", "id"));
        leadKeyType.setValue("email");
        leadKeyValue.setValue("");
        fieldList.setValue("email,firstName,lastName");
        sinceDateTime.setValue(new SimpleDateFormat(DATETIME_PATTERN_PARAM).format(new Date()));
    }

    @Override
    public void setupLayout() {
        Form main = new Form(this, Form.MAIN);
        main.addRow(leadKeyType);
        main.addColumn(leadKeyValue);
        main.addRow(sinceDateTime);
        main.addColumn(fieldList);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (getDatasetProperties() == null) {
            return;
        }
        form.getWidget(leadKeyType.getName()).setVisible(false);
        form.getWidget(leadKeyValue.getName()).setVisible(false);
        form.getWidget(sinceDateTime.getName()).setVisible(false);
        form.getWidget(fieldList.getName()).setVisible(false);
        switch (getDatasetProperties().operation.getValue()) {
        case getLeads:
            form.getWidget(leadKeyType.getName()).setVisible(true);
            form.getWidget(leadKeyValue.getName()).setVisible(true);
            break;
        case getLeadChanges:
            form.getWidget(sinceDateTime.getName()).setVisible(true);
            form.getWidget(fieldList.getName()).setVisible(true);
            break;
        case getLeadActivities:
            form.getWidget(sinceDateTime.getName()).setVisible(true);
            break;
        case getCustomObjects:
            break;
        }
    }

    @Override
    public TMarketoConnectionProperties getConnectionProperties() {
        return getDatasetProperties().getDatastoreProperties();
    }
}
