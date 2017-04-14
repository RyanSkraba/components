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
package org.talend.components.marketo.tmarketooutput;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.helpers.MarketoColumnMappingsTable;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class TMarketoOutputProperties extends MarketoComponentProperties {

    public enum OutputOperation {
        syncLead, // This operation requests an insert or update operation for a lead record.
        syncMultipleLeads, // This operation requests an insert or update operation for lead records in batch.
        deleteLeads, // REST only
        syncCustomObjects, // REST only
        deleteCustomObjects // REST only
    }

    public enum OperationType {
        createOnly,
        updateOnly,
        createOrUpdate,
        createDuplicate
    }

    public enum RESTLookupFields {
        id,
        cookie,
        email,
        twitterId,
        facebookId,
        linkedInId,
        sfdcAccountId,
        sfdcContactId,
        sfdcLeadId,
        sfdcLeadOwnerId,
        sfdcOpptyId
    }

    public Property<OutputOperation> outputOperation = newEnum("outputOperation", OutputOperation.class);

    public Property<OperationType> operationType = newEnum("operationType", OperationType.class);

    public Property<RESTLookupFields> lookupField = newEnum("lookupField", RESTLookupFields.class);

    /*
     * Select this check box to de-duplicate and update lead records using email address. Deselect this check box to
     * create another lead which contains the same email address.
     */
    public Property<Boolean> deDupeEnabled = newBoolean("deDupeEnabled");

    public MarketoColumnMappingsTable mappingInput = new MarketoColumnMappingsTable("mappingInput");

    /*
     * Custom Objects
     * 
     */

    public enum CustomObjectSyncAction {
        createOnly,
        updateOnly,
        createOrUpdate
    }

    public enum CustomObjectDeleteBy {
        idField,
        dedupeFields
    }

    public Property<String> customObjectName = newString("customObjectName").setRequired();

    public Property<CustomObjectSyncAction> customObjectSyncAction = newEnum("customObjectSyncAction",
            CustomObjectSyncAction.class);

    public Property<String> customObjectDedupeBy = newString("customObjectDedupeBy");

    public Property<CustomObjectDeleteBy> customObjectDeleteBy = newEnum("customObjectDeleteBy", CustomObjectDeleteBy.class);

    public Property<Boolean> deleteLeadsInBatch = newBoolean("deleteLeadsInBatch");

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(TMarketoOutputProperties.class);

    public TMarketoOutputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        Set<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(FLOW_CONNECTOR);
            connectors.add(REJECT_CONNECTOR);
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        outputOperation.setPossibleValues(OutputOperation.values());
        outputOperation.setValue(OutputOperation.syncLead);
        operationType.setPossibleValues(OperationType.values());
        operationType.setValue(OperationType.createOnly);

        lookupField.setPossibleValues(RESTLookupFields.values());
        lookupField.setValue(RESTLookupFields.email);

        deDupeEnabled.setValue(false);
        deleteLeadsInBatch.setValue(false);

        schemaInput.schema.setValue(MarketoConstants.getRESTOutputSchemaForSyncLead());

        setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                schemaFlow.schema.setValue(null);
                schemaReject.schema.setValue(null);
                updateSchemaRelated();
                refreshLayout(getForm(Form.MAIN));
            }
        });

        // Custom Objects
        customObjectName.setValue("");
        customObjectDeleteBy.setValue(CustomObjectDeleteBy.idField);
        customObjectDedupeBy.setValue("");
        customObjectSyncAction.setPossibleValues(CustomObjectSyncAction.values());
        customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(outputOperation);
        mainForm.addColumn(operationType);
        //
        mainForm.addColumn(customObjectSyncAction);
        mainForm.addColumn(customObjectDedupeBy);
        mainForm.addColumn(customObjectDeleteBy);
        mainForm.addRow(customObjectName);
        //
        mainForm.addRow(lookupField);
        mainForm.addRow(widget(mappingInput).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addRow(deDupeEnabled);
        mainForm.addRow(deleteLeadsInBatch);
        mainForm.addRow(batchSize);
        mainForm.addRow(dieOnError);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        boolean useSOAP = connection.apiMode.getValue().equals(APIMode.SOAP);

        if (form.getName().equals(Form.MAIN)) {
            // first, hide everything
            form.getWidget(mappingInput.getName()).setVisible(false);
            form.getWidget(operationType.getName()).setVisible(false);
            form.getWidget(lookupField.getName()).setVisible(false);
            form.getWidget(deDupeEnabled.getName()).setVisible(false);
            form.getWidget(batchSize.getName()).setVisible(false);
            form.getWidget(deleteLeadsInBatch.getName()).setVisible(false);
            //
            form.getWidget(customObjectSyncAction.getName()).setVisible(false);
            form.getWidget(customObjectName.getName()).setVisible(false);
            form.getWidget(customObjectDedupeBy.getName()).setVisible(false);
            form.getWidget(customObjectDeleteBy.getName()).setVisible(false);
            // batchSize
            if (outputOperation.getValue().equals(OutputOperation.syncMultipleLeads)) {
                form.getWidget(deDupeEnabled.getName()).setVisible(true);
                form.getWidget(batchSize.getName()).setVisible(true);
            }
            //
            if (useSOAP) {
                form.getWidget(mappingInput.getName()).setVisible(true);
            } else {
                switch (outputOperation.getValue()) {
                case syncLead:
                case syncMultipleLeads:
                    form.getWidget(deDupeEnabled.getName()).setVisible(true);
                    form.getWidget(operationType.getName()).setHidden(deDupeEnabled.getValue());
                    form.getWidget(lookupField.getName()).setHidden(deDupeEnabled.getValue());
                    break;
                case deleteLeads:
                    form.getWidget(deleteLeadsInBatch.getName()).setVisible(true);
                    form.getWidget(batchSize.getName()).setVisible(deleteLeadsInBatch.getValue());
                    break;
                case syncCustomObjects:
                    form.getWidget(customObjectName.getName()).setVisible(true);
                    form.getWidget(customObjectSyncAction.getName()).setVisible(true);
                    form.getWidget(customObjectDedupeBy.getName()).setVisible(true);
                    break;
                case deleteCustomObjects:
                    form.getWidget(customObjectName.getName()).setVisible(true);
                    form.getWidget(customObjectDeleteBy.getName()).setVisible(true);
                    break;
                }
            }
        }
    }

    public ValidationResult validateOutputOperation() {
        if (connection.apiMode.getValue().equals(APIMode.SOAP)) {
            switch (outputOperation.getValue()) {
            case syncLead:
            case syncMultipleLeads:
                return ValidationResult.OK;
            case deleteLeads:
            case syncCustomObjects:
            case deleteCustomObjects:
                ValidationResult vr = new ValidationResult();
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("validation.error.operation.soap"));
                return vr;
            }
        }
        return ValidationResult.OK;
    }

    public void afterApiMode() {
        if (connection.apiMode.getValue().equals(APIMode.SOAP)) {
            schemaInput.schema.setValue(MarketoConstants.getSOAPOuputSchemaForSyncLead());
        } else {
            schemaInput.schema.setValue(MarketoConstants.getRESTOutputSchemaForSyncLead());
        }
        afterOutputOperation();
    }

    public void afterOutputOperation() {
        if (connection.apiMode.getValue().equals(APIMode.SOAP)) {
            schemaInput.schema.setValue(MarketoConstants.getSOAPOuputSchemaForSyncLead());
        } else {
            switch (outputOperation.getValue()) {
            case syncLead:
            case syncMultipleLeads:
                schemaInput.schema.setValue(MarketoConstants.getRESTOutputSchemaForSyncLead());
                break;
            case deleteLeads:
                schemaInput.schema.setValue(MarketoConstants.getDeleteLeadsSchema());
                break;
            case syncCustomObjects:
            case deleteCustomObjects:
                schemaInput.schema.setValue(MarketoConstants.getCustomObjectSyncSchema());
                break;
            }
        }
        updateSchemaRelated();
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterDeDupeEnabled() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterDeleteLeadsInBatch() {
        updateOutputSchemas();
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterBatchSize() {
        updateOutputSchemas();
        refreshLayout(getForm(Form.MAIN));
    }

    public void updateSchemaRelated() {
        updateMappings();
        updateOutputSchemas();
    }

    public void updateMappings() {
        List<String> fld = getSchemaFields();
        mappingInput.columnName.setValue(fld);
        // protect mappings...
        if (fld.size() != mappingInput.size()) {
            List<String> mcn = new ArrayList<>();
            for (String t : fld) {
                mcn.add("");
            }
            mappingInput.marketoColumnName.setValue(mcn);
        }
    }

    protected void updateOutputSchemas() {
        Schema inputSchema = schemaInput.schema.getValue();
        inputSchema.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        // batch processing workaround - studio can't handle feedbacks correctly on close call.
        // so we set an empty schema for now.
        // seems that it may be resolved : https://jira.talendforge.org/browse/TDI-38603
        if ((outputOperation.getValue().equals(OutputOperation.deleteLeads) && deleteLeadsInBatch.getValue())
                || (outputOperation.getValue().equals(OutputOperation.syncMultipleLeads) && batchSize.getValue() > 1)) {
            schemaFlow.schema.setValue(MarketoConstants.getEmptySchema());
            schemaReject.schema.setValue(MarketoConstants.getEmptySchema());
            return;
        }
        //
        boolean isCustomObject = (outputOperation.getValue().equals(OutputOperation.syncCustomObjects)
                || outputOperation.getValue().equals(OutputOperation.deleteCustomObjects));
        //
        final List<Field> flowFields = new ArrayList<Field>();
        final List<Field> rejectFields = new ArrayList<Field>();
        Field f;
        f = new Field("Status", Schema.create(Type.STRING), null, (Object) null);
        f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        flowFields.add(f);
        if (isCustomObject) {
            if (inputSchema.getField(MarketoConstants.FIELD_MARKETO_GUID) == null) {
                f = new Field(MarketoConstants.FIELD_MARKETO_GUID, Schema.create(Type.STRING), null, (Object) null);
                f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
                f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
                flowFields.add(f);
            }
            f = new Field(MarketoConstants.FIELD_SEQ, Schema.create(Type.INT), null, (Object) null);
            f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
            f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
            flowFields.add(f);
            f = new Field(MarketoConstants.FIELD_REASON, Schema.create(Type.STRING), null, (Object) null);
            f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
            f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
            flowFields.add(f);
        }
        //
        f = new Field("Status", Schema.create(Type.STRING), null, (Object) null);
        f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        rejectFields.add(f);
        f = new Field("ERROR_MSG", Schema.create(Schema.Type.STRING), null, (Object) null);
        f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        rejectFields.add(f);

        Schema flowSchema = newSchema(inputSchema, "schemaFlow", flowFields);
        Schema rejectSchema = newSchema(inputSchema, "schemaReject", rejectFields);
        schemaFlow.schema.setValue(flowSchema);
        schemaReject.schema.setValue(rejectSchema);
    }

}
