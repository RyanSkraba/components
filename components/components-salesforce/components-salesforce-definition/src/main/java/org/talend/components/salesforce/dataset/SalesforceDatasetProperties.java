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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.salesforce.common.SalesforceErrorCodes;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreDefinition;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceDatasetProperties.class);

    public ReferenceProperties<SalesforceDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            SalesforceDatastoreDefinition.NAME);

    public Property<SourceType> sourceType = PropertyFactory.newEnum("sourceType", SourceType.class).setRequired();

    public StringProperty moduleName = PropertyFactory.newString("moduleName");

    public Property<String> query = PropertyFactory.newString("query");

    public SchemaProperties main = new SchemaProperties("main");

    public SalesforceDatasetProperties(String name) {
        super(name);
    }

    private void retrieveModules() throws IOException {
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
            }
        }
    }

    public void afterSourceType() throws IOException {
        // refresh the module list
        retrieveModules();
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

    /**
     * the method is called back at many places, even some strange places, so it should work only for basic layout, not some
     * action which need runtime support.
     */
    @Override
    public void refreshLayout(Form form) {
        if (sourceType.getValue() == SourceType.MODULE_SELECTION) {
            form.getWidget(moduleName).setVisible(true);
            moduleName.setRequired(true);
            //We can not have a hidden field which is required
            form.getWidget(query).setVisible(false);
            query.setRequired(false);
        }
        else if (sourceType.getValue() == SourceType.SOQL_QUERY) {
            form.getWidget(query).setVisible(true);
            query.setRequired();
            //We can not have a hidden field which is required
            form.getWidget(moduleName).setVisible(false);
            moduleName.setRequired(false);
        }
        super.refreshLayout(form);
    }

    @Override
    public SalesforceDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(SalesforceDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
        try {
            retrieveModules();
        }
        catch(IOException e) {
            LOGGER.error("error getting salesforce modules", e);
            throw new TalendRuntimeException(SalesforceErrorCodes.UNABLE_TO_RETRIEVE_MODULES, e);
        }
    }

    public enum SourceType {
        MODULE_SELECTION,
        SOQL_QUERY
    }

}
