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
package org.talend.components.marketo.wizard;

import static org.talend.components.marketo.MarketoComponentDefinition.RUNTIME_SOURCEORSINK_CLASS;
import static org.talend.components.marketo.MarketoComponentDefinition.getSandboxedInstance;
import static org.talend.components.marketo.MarketoConstants.getEmptySchema;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.MarketoUtils;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkRuntime;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkSchemaProvider;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.sandbox.SandboxedInstance;

public class MarketoComponentWizardBaseProperties extends MarketoComponentProperties {

    public static final String FORM_FETCH_LEAD_SCHEMA = "fetchLeadSchema";

    public Property<String> customObjectName = newString("customObjectName").setRequired();

    public Property<InputOperation> inputOperation = newEnum("inputOperation", InputOperation.class).setRequired();

    public Property<CustomObjectAction> customObjectAction = newEnum("customObjectAction", CustomObjectAction.class);

    public Property<OutputOperation> outputOperation = newEnum("outputOperation", OutputOperation.class);

    public Property<CustomObjectSyncAction> customObjectSyncAction = newEnum("customObjectSyncAction",
            CustomObjectSyncAction.class);

    public transient PresentationItem fetchLeadSchema = new PresentationItem("fetchLeadSchema", "Select Lead schema");

    public Property<List<NamedThing>> selectedLeadColumns = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedLeadColumns");

    public Map<String, Field> allAvailableleadFields = new LinkedHashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(MarketoComponentWizardBaseProperties.class);

    public MarketoComponentWizardBaseProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        return null;
    }

    public void beforeFormPresentFetchLeadSchema() throws IOException {
        List<NamedThing> cols = new ArrayList<>();
        SandboxedInstance sandboxedInstance = getRuntimeSandboxedInstance();
        MarketoSourceOrSinkSchemaProvider sos = (MarketoSourceOrSinkSchemaProvider) sandboxedInstance.getInstance();
        sos.initialize(null, this);
        ValidationResult vr = ((MarketoSourceOrSinkRuntime) sos).validateConnection(this);
        if (!Result.OK.equals(vr.getStatus())) {
            throw new IOException(vr.getMessage());
        }
        for (Field f : sos.getAllLeadFields()) {
            allAvailableleadFields.put(f.name(), f);
        }
        SimpleNamedThing snt;
        // add current fields in schema
        List<NamedThing> currentSchema = new ArrayList<>();
        for (Field f : schemaInput.schema.getValue().getFields()) {
            snt = new SimpleNamedThing(f.name(), f.name());
            cols.add(snt);
            currentSchema.add(snt);
        }
        for (String f : allAvailableleadFields.keySet()) {
            snt = new SimpleNamedThing(f, f);
            if (!cols.contains(snt)) {
                cols.add(snt);
            }
        }
        selectedLeadColumns.setPossibleValues(cols);
        selectedLeadColumns.setValue(currentSchema);
    }

    public void afterFetchLeadSchema() {
        List<Field> newFields = new ArrayList<>();
        try {
            for (NamedThing nl : selectedLeadColumns.getValue()) {
                newFields.add(MarketoUtils.generateNewField(allAvailableleadFields.get(nl.getName())));
            }
            Schema s = newSchema(getEmptySchema(), "selectedLeadFields", newFields);
            schemaInput.schema.setValue(s);
            // cleanup allAvailableleadFields
            allAvailableleadFields.clear();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public enum InputOperation {
        getLead, // retrieves basic information of leads and lead activities in Marketo DB. getLead:
        getMultipleLeads, // retrieves lead records in batch.
        getLeadActivity, // retrieves the history of activity records for a single lead identified by the provided key.
        getLeadChanges, // checks the changes on Lead data in Marketo DB.
        CustomObject // CO Operation
    }

    /**
     * Custom objects
     */
    public enum CustomObjectAction {
        describe,
        list,
        get
    }

    public enum OutputOperation {
        syncLead, // This operation requests an insert or update operation for a lead record.
        syncMultipleLeads, // This operation requests an insert or update operation for lead records in batch.
        deleteLeads, // REST only
        syncCustomObjects, // REST only
        deleteCustomObjects // REST only
    }

    public enum CustomObjectSyncAction {
        createOnly,
        updateOnly,
        createOrUpdate
    }

    protected SandboxedInstance getRuntimeSandboxedInstance() {
        return getSandboxedInstance(RUNTIME_SOURCEORSINK_CLASS);
    }

}
