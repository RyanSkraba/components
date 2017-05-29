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

package org.talend.components.netsuite.output;

import static org.talend.components.netsuite.util.ComponentExceptions.exceptionToValidationResult;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;

import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.common.SchemaProperties;
import org.talend.components.netsuite.NetSuiteComponentDefinition;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteModuleProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.components.netsuite.util.ComponentExceptions;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.java8.Function;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.PostDeserializeSetup;

/**
 * NetSuite Output component's Properties which holds information about
 * target record type, output options and outgoing flows.
 */
public class NetSuiteOutputModuleProperties extends NetSuiteModuleProperties {

    public final Property<OutputAction> action = newEnum("action", OutputAction.class);

    public final Property<Boolean> useNativeUpsert = newBoolean("useNativeUpsert");

    public final SchemaProperties flowSchema;

    public final SchemaProperties rejectSchema;

    public transient final PresentationItem syncOutgoingSchema = new PresentationItem(
            "syncOutgoingSchema", "Sync outgoing schema");

    /**
     * Holds main schema after last update to avoid updating of outgoing schemas
     * due to repeating {@code afterSchema} invocations in {@link SchemaProperties}.
     *
     * <p>Used in design time only.
     */
    private transient Schema lastMainSchema;

    public NetSuiteOutputModuleProperties(String name, NetSuiteConnectionProperties connectionProperties) {
        super(name, connectionProperties);

        flowSchema = new SchemaProperties("flowSchema");
        rejectSchema = new SchemaProperties("rejectSchema");
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        action.setValue(OutputAction.ADD);
        useNativeUpsert.setValue(Boolean.FALSE);

        setupMainSchemaListener();
    }

    private void setupMainSchemaListener() {
        main.setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                afterMainSchema();
            }
        });
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        final Form mainSchemaForm = main.getForm(Form.REFERENCE);

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(widget(moduleName)
                .setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE)
                .setLongRunning(true));
        mainForm.addRow(mainSchemaForm);
        mainForm.addRow(widget(action)
                .setLongRunning(true));
        mainForm.addRow(widget(syncOutgoingSchema)
                .setWidgetType(Widget.BUTTON_WIDGET_TYPE)
                .setLongRunning(true));

        Form advForm = Form.create(this, Form.ADVANCED);
        advForm.addRow(connection.getForm(Form.ADVANCED));
        advForm.addRow(useNativeUpsert);

        Form refForm = Form.create(this, Form.REFERENCE);
        refForm.addRow(widget(moduleName)
                .setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE)
                .setLongRunning(true));
        refForm.addRow(mainSchemaForm);
        refForm.addRow(widget(action)
                .setLongRunning(true));
        refForm.addRow(widget(syncOutgoingSchema)
                .setWidgetType(Widget.BUTTON_WIDGET_TYPE)
                .setLongRunning(true));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(syncOutgoingSchema).setHidden(false);
        } else if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(useNativeUpsert.getName()).setHidden(action.getValue() != OutputAction.UPSERT);
        }
    }

    public ValidationResult beforeModuleName() throws Exception {
        // Before selecting of a target record type we should provide
        // set of record types that are available for work.
        try {
            List<NamedThing> types = getRecordTypes();
            moduleName.setPossibleNamedThingValues(types);

            return ValidationResult.OK;
        } catch (TalendRuntimeException ex) {
            return exceptionToValidationResult(ex);
        }
    }

    public ValidationResult afterModuleName() throws Exception {
        // After selecting of target record type we should:
        // - Set up main schema which will be used for records emitted by component
        // - Set up schema for outgoing flows (normal and reject flows)
        try {
            setupSchema();
            setupOutgoingSchema();

            refreshLayout(getForm(Form.MAIN));
            refreshLayout(getForm(Form.ADVANCED));

            return ValidationResult.OK;
        } catch (TalendRuntimeException e) {
            return exceptionToValidationResult(e);
        }
    }

    public ValidationResult afterAction() {
        try {
            setupSchema();
            setupOutgoingSchema();

            refreshLayout(getForm(Form.MAIN));
            refreshLayout(getForm(Form.ADVANCED));

            return ValidationResult.OK;
        } catch (TalendRuntimeException e) {
            return exceptionToValidationResult(e);
        }
    }

    public ValidationResult validateSyncOutgoingSchema() {
        try {
            setupOutgoingSchema();

            refreshLayout(getForm(Form.MAIN));
            refreshLayout(getForm(Form.ADVANCED));

            return ValidationResult.OK;
        } catch (TalendRuntimeException e) {
            return exceptionToValidationResult(e);
        }
    }

    private void afterMainSchema() {
        try {
            Schema schema = main.schema.getValue();

            // If last main schema is null then we treat this as initial update (setup)
            // of schema properties after materialization/deserialization.
            // On initial update we skip updating of schema for outgoing flow(s) and
            // just remember initial schema.
            // On subsequent updates we should check whether schema was changed and
            // update schema for outgoing flow(s).

            if (lastMainSchema != null) {

                // If schema was not changed since last known update then we can
                // ignore this change to avoid unnecessary updating of schema for outgoing flow(s).
                if (schema.equals(lastMainSchema)) {
                    return;
                }

                setupOutgoingSchema();

                refreshLayout(getForm(Form.MAIN));
                refreshLayout(getForm(Form.ADVANCED));
            }

            // Remember changed schema for next check
            lastMainSchema = schema;

        } catch (TalendRuntimeException e) {
            throw ComponentExceptions.asComponentExceptionWithValidationResult(e);
        }
    }

    /**
     * Get schema for update <code>success</code> flow.
     *
     * @param typeName name of object's type
     * @param schema source schema to be modified
     * @return schema
     */
    public Schema getSchemaForUpdateFlow(final String typeName, final Schema schema) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {
            @Override public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchemaForUpdateFlow(typeName, schema);
            }
        });
    }

    /**
     * Get schema for delete <code>success</code> flow.
     *
     * @param typeName name of object's type
     * @param schema source schema to be modified
     * @return schema
     */
    public Schema getSchemaForDeleteFlow(final String typeName, final Schema schema) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {
            @Override public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchemaForDeleteFlow(typeName, schema);
            }
        });
    }

    /**
     * Get schema for update <code>reject</code> flow.
     *
     * @param typeName name of object's type
     * @param schema source schema to be modified
     * @return schema
     */
    public Schema getSchemaForUpdateReject(final String typeName, final Schema schema) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {
            @Override public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchemaForUpdateReject(typeName, schema);
            }
        });
    }

    /**
     * Get schema for delete <code>reject</code> flow.
     *
     * @param typeName name of object's type
     * @param schema source schema to be modified
     * @return schema
     */
    public Schema getSchemaForDeleteReject(final String typeName, final Schema schema) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {
            @Override public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchemaForDeleteReject(typeName, schema);
            }
        });
    }

    /**
     * Set up main schema for component.
     *
     * <p>For updating and deletion schemas are different.
     */
    private void setupSchema() {
        switch (action.getValue()) {
        case ADD:
        case UPDATE:
        case UPSERT:
            setupSchemaForUpdate();
            break;
        case DELETE:
            setupSchemaForDelete();
            break;
        }
    }

    /**
     * Set up schema for outgoing flows.
     *
     * <p>For updating and deletion schemas are different.
     */
    private void setupOutgoingSchema() {
        flowSchema.schema.setValue(null);
        rejectSchema.schema.setValue(null);

        switch (action.getValue()) {
        case ADD:
        case UPDATE:
        case UPSERT:
            setupOutgoingSchemaForUpdate();
            break;
        case DELETE:
            setupOutgoingSchemaForDelete();
            break;
        }
    }

    /**
     * Set up main schema for <i>Add/Update/Upsert</i> output action.
     */
    private void setupSchemaForUpdate() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = getSchemaForUpdate(typeName);
        main.schema.setValue(schema);
    }

    /**
     * Set up outgoing flow schema for <i>Add/Update/Upsert</i> output action.
     */
    private void setupOutgoingSchemaForUpdate() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = main.schema.getValue();
        Schema successFlowSchema = getSchemaForUpdateFlow(typeName, schema);
        Schema rejectFlowSchema = getSchemaForUpdateReject(typeName, schema);

        flowSchema.schema.setValue(successFlowSchema);
        rejectSchema.schema.setValue(rejectFlowSchema);
    }

    /**
     * Set up main schema for <i>Delete</i> output action.
     */
    private void setupSchemaForDelete() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = getSchemaForDelete(typeName);
        main.schema.setValue(schema);
    }

    /**
     * Set up outgoing flow schema for <i>Delete</i> output action.
     */
    protected void setupOutgoingSchemaForDelete() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = main.schema.getValue();
        Schema successFlowSchema = getSchemaForDeleteFlow(typeName, schema);
        Schema rejectFlowSchema = getSchemaForDeleteReject(typeName, schema);

        flowSchema.schema.setValue(successFlowSchema);
        rejectSchema.schema.setValue(rejectFlowSchema);
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean result = super.postDeserialize(version, setup, persistent);

        // Restore main schema listener after materialization
        setupMainSchemaListener();

        return result;
    }

}
