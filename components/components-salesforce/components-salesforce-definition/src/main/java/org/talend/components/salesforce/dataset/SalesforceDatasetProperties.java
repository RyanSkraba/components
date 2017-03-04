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
package org.talend.components.salesforce.dataset;

import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreDefinition;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class SalesforceDatasetProperties extends PropertiesImpl implements DatasetProperties<SalesforceDatastoreProperties> {

    public ReferenceProperties<SalesforceDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            SalesforceDatastoreDefinition.NAME);

    public Property<SourceType> sourceType = PropertyFactory.newEnum("sourceType", SourceType.class);

    public Property<String> moduleName = PropertyFactory.newString("moduleName");

    public Property<String> query = PropertyFactory.newString("query");

    public SchemaProperties main = new SchemaProperties("main");

    public SalesforceDatasetProperties(String name) {
        super(name);
    }

    public void afterSourceType() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void setupProperties() {
        sourceType.setValue(SourceType.SOQL_QUERY);
        query.setValue("SELECT Id, Name FROM Account");
    }

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, Form.MAIN);

        mainForm.addRow(sourceType);
        mainForm.addRow(moduleName);
        mainForm.addRow(Widget.widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        form.getWidget(moduleName).setVisible(sourceType.getValue() == SourceType.MODULE_SELECTION);

        form.getWidget(query).setVisible(sourceType.getValue() == SourceType.SOQL_QUERY);
    }

    @Override
    public SalesforceDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(SalesforceDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
    }

    public enum SourceType {
        MODULE_SELECTION,
        SOQL_QUERY
    }

}
