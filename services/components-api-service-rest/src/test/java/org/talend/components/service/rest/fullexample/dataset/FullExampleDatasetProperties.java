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
package org.talend.components.service.rest.fullexample.dataset;

import java.io.IOException;

import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.service.rest.fullexample.datastore.FullExampleDatastoreDefinition;
import org.talend.components.service.rest.fullexample.datastore.FullExampleDatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.property.StringProperty;

public class FullExampleDatasetProperties extends PropertiesImpl implements DatasetProperties<FullExampleDatastoreProperties> {

    /**
     * FullExampleDatasetProperties uses salesforce's dataset properties for tests
     */
    private static final long serialVersionUID = -8035880860245867110L;

    public ReferenceProperties<FullExampleDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            FullExampleDatastoreDefinition.NAME);

    public Property<SourceType> sourceType = PropertyFactory.newEnum("sourceType", SourceType.class).setRequired();

    public StringProperty moduleName = PropertyFactory.newString("moduleName");

    public Property<String> query = PropertyFactory.newString("query");

    public SchemaProperties main = new SchemaProperties("main");

    public Property<String> testAfterDatastoreTrigger = PropertyFactory.newString("testAfterDatastoreTrigger").setValue("foo");

    public FullExampleDatasetProperties(String name) {
        super(name);
    }

    public void afterSourceType() throws IOException {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void setupProperties() {
        sourceType.setValue(SourceType.MODULE_SELECTION);
    }

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, Form.MAIN);

        mainForm.addRow(Widget.widget(sourceType).setWidgetType(Widget.RADIO_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(moduleName).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        if (sourceType.getValue() == SourceType.MODULE_SELECTION) {
            form.getWidget(moduleName).setVisible(true);
            moduleName.setRequired(true);
            // We can not have a hidden field which is required
            form.getWidget(query).setVisible(false);
            query.setRequired(false);
        } else if (sourceType.getValue() == SourceType.SOQL_QUERY) {
            form.getWidget(query).setVisible(true);
            query.setRequired();
            // We can not have a hidden field which is required
            form.getWidget(moduleName).setVisible(false);
            moduleName.setRequired(false);
        }
        super.refreshLayout(form);
    }

    @Override
    public FullExampleDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(FullExampleDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
    }

    public enum SourceType {
        MODULE_SELECTION,
        SOQL_QUERY
    }

    public void afterDatastore() {
        testAfterDatastoreTrigger.setValue("bar");
    }
}
