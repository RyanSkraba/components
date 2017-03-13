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

import java.io.IOException;
import java.util.List;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreDefinition;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.property.StringProperty;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class SalesforceDatasetProperties extends PropertiesImpl implements DatasetProperties<SalesforceDatastoreProperties> {

    /**
     * 
     */
    private static final long serialVersionUID = -8035880860245867110L;

    public ReferenceProperties<SalesforceDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            SalesforceDatastoreDefinition.NAME);

    public Property<SourceType> sourceType = PropertyFactory.newEnum("sourceType", SourceType.class).setRequired();

    public StringProperty moduleName = PropertyFactory.newString("moduleName");

    public Property<String> query = PropertyFactory.newString("query").setRequired();

    public SchemaProperties main = new SchemaProperties("main");

    public SalesforceDatasetProperties(String name) {
        super(name);
    }

    public void afterSourceType() throws IOException {
        refreshLayout(getForm(Form.MAIN));

        // refresh the module list
        if (sourceType.getValue() == SourceType.MODULE_SELECTION) {
            ClassLoader classLoader = this.getClass().getClassLoader();
            RuntimeInfo runtimeInfo = new JarRuntimeInfo("mvn:org.talend.components/components-salesforce-runtime",
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-salesforce-runtime"),
                    "org.talend.components.salesforce.runtime.dataprep.SalesforceDataprepSource");
            try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, classLoader)) {
                SalesforceRuntimeSourceOrSink runtime = (SalesforceRuntimeSourceOrSink) sandboxedInstance.getInstance();
                
                SalesforceInputProperties properties = new SalesforceInputProperties("model");
                properties.setDatasetProperties(this);
                
                runtime.initialize(null, properties);
                List<NamedThing> moduleNames = runtime.getSchemaNames(null);
                moduleName.setPossibleNamedThingValues(moduleNames);
                moduleName.setRequired(true);
            }
        }
    }

    @Override
    public void setupProperties() {
        sourceType.setValue(SourceType.SOQL_QUERY);
        query.setValue("SELECT Id, Name FROM Account");
    }

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, Form.MAIN);

        mainForm.addRow(Widget.widget(sourceType).setWidgetType(Widget.RADIO_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(moduleName).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));
    }

    /**
     * the method is called back at many places, even some strange places, so it should work only for basic layout, not some
     * action which need runtime support.
     */
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
