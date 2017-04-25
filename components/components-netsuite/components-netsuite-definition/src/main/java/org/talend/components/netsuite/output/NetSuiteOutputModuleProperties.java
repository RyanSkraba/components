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
import org.talend.components.common.SchemaProperties;
import org.talend.components.netsuite.NetSuiteComponentDefinition;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteModuleProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.java8.Function;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 *
 */
public class NetSuiteOutputModuleProperties extends NetSuiteModuleProperties {

    public final Property<OutputAction> action = newEnum("action", OutputAction.class);

    public final Property<Boolean> useNativeUpsert = newBoolean("useNativeUpsert");

    public final SchemaProperties flowSchema;

    public final SchemaProperties rejectSchema;

    public transient final PresentationItem syncOutgoingSchema = new PresentationItem(
            "syncOutgoingSchema", "Sync outgoing schema");

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
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(widget(moduleName)
                .setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE)
                .setLongRunning(true));
        mainForm.addRow(main.getForm(Form.REFERENCE));
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
        refForm.addRow(main.getForm(Form.REFERENCE));
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
        try {
            List<NamedThing> types = getRecordTypes();
            moduleName.setPossibleNamedThingValues(types);

            return ValidationResult.OK;
        } catch (Exception ex) {
            return exceptionToValidationResult(ex);
        }
    }

    public ValidationResult afterModuleName() throws Exception {
        try {
            setupSchema();
            setupOutgoingSchema();

            refreshLayout(getForm(Form.MAIN));
            refreshLayout(getForm(Form.ADVANCED));

            return ValidationResult.OK;
        } catch (Exception e) {
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
        } catch (Exception e) {
            return exceptionToValidationResult(e);
        }
    }

    public ValidationResult validateSyncOutgoingSchema() {
        try {
            setupOutgoingSchema();

            refreshLayout(getForm(Form.MAIN));
            refreshLayout(getForm(Form.ADVANCED));

            return ValidationResult.OK;
        } catch (Exception e) {
            return exceptionToValidationResult(e);
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

    protected void setupSchema() {
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

    protected void setupOutgoingSchema() {
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

    protected void setupSchemaForUpdate() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = getSchemaForUpdate(typeName);
        main.schema.setValue(schema);
    }

    protected void setupOutgoingSchemaForUpdate() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = main.schema.getValue();
        Schema successFlowSchema = getSchemaForUpdateFlow(typeName, schema);
        Schema rejectFlowSchema = getSchemaForUpdateReject(typeName, schema);

        flowSchema.schema.setValue(successFlowSchema);
        rejectSchema.schema.setValue(rejectFlowSchema);
    }

    protected void setupSchemaForDelete() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = getSchemaForDelete(typeName);
        main.schema.setValue(schema);
    }

    protected void setupOutgoingSchemaForDelete() {
        assertModuleName();

        final String typeName = moduleName.getStringValue();

        Schema schema = main.schema.getValue();
        Schema successFlowSchema = getSchemaForDeleteFlow(typeName, schema);
        Schema rejectFlowSchema = getSchemaForDeleteReject(typeName, schema);

        flowSchema.schema.setValue(successFlowSchema);
        rejectSchema.schema.setValue(rejectFlowSchema);
    }
}
