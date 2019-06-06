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

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoComponentDefinition.RUNTIME_SOURCE_CLASS;
import static org.talend.components.marketo.MarketoComponentDefinition.USE_CURRENT_JVM_PROPS;
import static org.talend.components.marketo.MarketoComponentDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkRuntime;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkSchemaProvider;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.sandbox.SandboxedInstance;

public class MarketoDatasetProperties extends ComponentPropertiesImpl implements DatasetProperties<MarketoDatastoreProperties> {

    public ReferenceProperties<MarketoDatastoreProperties> datastore = new ReferenceProperties<>("datastore",
            MarketoDatastoreDefinition.COMPONENT_NAME);

    public enum Operation {
        getLeads,
        getLeadChanges,
        getLeadActivities,
        getCustomObjects
    }

    public Property<Operation> operation = newEnum("operation", Operation.class).setRequired();

    public Property<String> customObjectName = newString("customObjectName").setRequired();

    public Property<String> filterType = newString("filterType").setRequired();

    public Property<String> filterValue = newString("filterValue").setRequired();

    public Property<Integer> batchSize = newInteger("batchSize");

    public SchemaProperties main = new SchemaProperties("main");

    private transient static final Logger LOG = getLogger(MarketoDatasetProperties.class);

    public MarketoDatasetProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        operation.setPossibleValues(Operation.values());
        operation.setValue(Operation.getLeads);
        setupSchema();
        customObjectName.setValue("");
        filterType.setValue("");
        filterValue.setValue("");
        batchSize.setValue(200);
    }

    public Schema getSchema() {
        return main.schema.getValue();
    }

    public void setupSchema() {
        switch (operation.getValue()) {
        case getLeads:
            main.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
            break;
        case getLeadChanges:
            main.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadChanges());
            break;
        case getLeadActivities:
            main.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadActivity());
            break;
        case getCustomObjects:
            main.schema.setValue(MarketoConstants.getCustomObjectRecordSchema());
            break;
        }
    }

    @Override
    public void setupLayout() {
        Form main = new Form(this, Form.MAIN);
        main.addRow(Widget.widget(operation).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        main.addRow(Widget.widget(customObjectName).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        main.addRow(filterType);
        main.addColumn(filterValue);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        boolean visible = operation.getValue().equals(Operation.getCustomObjects);
        form.getWidget(customObjectName.getName()).setVisible(visible);
        form.getWidget(filterType.getName()).setVisible(visible);
        form.getWidget(filterValue.getName()).setVisible(visible);
    }

    public void afterOperation() {
        setupSchema();
        if (operation.getValue().equals(Operation.getCustomObjects)) {
            retrieveCustomObjectList();
        }
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterCustomObjectName() {
        retrieveCustomObjectSchema();
        refreshLayout(getForm(Form.MAIN));
    }

    public void retrieveCustomObjectList() {
        List<NamedThing> coList = new ArrayList<>();
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(RUNTIME_SOURCE_CLASS, USE_CURRENT_JVM_PROPS)) {
            MarketoSourceOrSinkRuntime sos = (MarketoSourceOrSinkRuntime) sandboxedInstance.getInstance();
            try {
                checkUnsuccessfulValidation(sos.initialize(null, getDatastoreProperties()));
                checkUnsuccessfulValidation(sos.validate(null));
                coList = ((MarketoSourceOrSinkSchemaProvider) sos).getSchemaNames(null);
            } catch (RuntimeException | IOException e) {
                LOG.error("[retrieveCustomObjectList] retrieve error : {}.", e.getMessage());
            }
        }
        customObjectName.setPossibleValues(coList.stream().map((co) -> co.getName()).collect(Collectors.toList()));
    }

    public void retrieveCustomObjectSchema() {
        String coName = customObjectName.getValue();
        if (StringUtils.isEmpty(coName)) {
            main.schema.setValue(MarketoConstants.getCustomObjectRecordSchema());
            return;
        }
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(RUNTIME_SOURCE_CLASS, USE_CURRENT_JVM_PROPS)) {
            MarketoSourceOrSinkRuntime sos = (MarketoSourceOrSinkRuntime) sandboxedInstance.getInstance();
            try {
                checkUnsuccessfulValidation(sos.initialize(null, getDatastoreProperties()));
                checkUnsuccessfulValidation(sos.validate(null));
                main.schema.setValue(((MarketoSourceOrSinkSchemaProvider) sos).getSchemaForCustomObject(coName));
            } catch (RuntimeException | IOException e) {
                LOG.error("[retrieveCustomObjectSchema] retrieve error for {} : {}.", coName, e.getMessage());
                main.schema.setValue(MarketoConstants.getCustomObjectRecordSchema());
            }
        }
    }

    private void checkUnsuccessfulValidation(ValidationResult result) {
        if (result == null || result.getStatus() == Result.OK) {
            return;
        }
        throw TalendRuntimeException.createUnexpectedException(result.getMessage());
    }

    @Override
    public MarketoDatastoreProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(MarketoDatastoreProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
    }
}
