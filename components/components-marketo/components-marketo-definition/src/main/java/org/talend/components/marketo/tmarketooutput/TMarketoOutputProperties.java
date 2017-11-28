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

import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.deleteCustomObjects;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.deleteLeads;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncCustomObjects;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncLead;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation.syncMultipleLeads;
import static org.talend.daikon.properties.ValidationResult.OK;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoUtils;
import org.talend.components.marketo.helpers.MarketoColumnMappingsTable;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.migration.SerializeSetVersion;

public class TMarketoOutputProperties extends MarketoComponentWizardBaseProperties implements SerializeSetVersion {

    public Property<Integer> batchSize = newInteger("batchSize");

    public Property<Boolean> dieOnError = newBoolean("dieOnError");

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

    public Property<OperationType> operationType = newEnum("operationType", OperationType.class);

    public Property<RESTLookupFields> lookupField = newEnum("lookupField", RESTLookupFields.class);

    /*
     * Select this check box to de-duplicate and update lead records using email address. Deselect this check box to create
     * another lead which contains the same email address.
     */
    public Property<Boolean> deDupeEnabled = newBoolean("deDupeEnabled");

    public MarketoColumnMappingsTable mappingInput = new MarketoColumnMappingsTable("mappingInput");

    /*
     * Custom Objects
     * 
     */

    public enum CustomObjectDeleteBy {
        idField,
        dedupeFields
    }

    public Property<String> customObjectDedupeBy = newString("customObjectDedupeBy");

    public Property<CustomObjectDeleteBy> customObjectDeleteBy = newEnum("customObjectDeleteBy", CustomObjectDeleteBy.class);

    public Property<Boolean> deleteLeadsInBatch = newBoolean("deleteLeadsInBatch");

    private static final Logger LOG = LoggerFactory.getLogger(TMarketoOutputProperties.class);

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

        batchSize.setValue(100);
        dieOnError.setValue(true);

        outputOperation.setPossibleValues((Object[]) OutputOperation.values());
        outputOperation.setValue(syncLead);
        operationType.setPossibleValues((Object[]) OperationType.values());
        operationType.setValue(OperationType.createOnly);
        lookupField.setPossibleValues((Object[]) RESTLookupFields.values());
        lookupField.setValue(RESTLookupFields.email);
        deDupeEnabled.setValue(false);
        deleteLeadsInBatch.setValue(false);
        // Custom Objects
        customObjectDeleteBy.setValue(CustomObjectDeleteBy.idField);
        customObjectDedupeBy.setValue("");
        customObjectSyncAction.setPossibleValues((Object[]) CustomObjectSyncAction.values());
        customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
        //
        schemaInput.schema.setValue(MarketoConstants.getRESTOutputSchemaForSyncLead());
        beforeMappingInput();
        setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                schemaFlow.schema.setValue(null);
                schemaReject.schema.setValue(null);
                beforeMappingInput();
                updateOutputSchemas();
            }
        });
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
            if (outputOperation.getValue().equals(syncMultipleLeads)) {
                form.getWidget(deDupeEnabled.getName()).setVisible(true);
                form.getWidget(batchSize.getName()).setVisible(true);
            }
            //
            if (isApiSOAP()) {
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
        if (isApiSOAP()) {
            switch (outputOperation.getValue()) {
            case syncLead:
            case syncMultipleLeads:
                return OK;
            case deleteLeads:
            case syncCustomObjects:
            case deleteCustomObjects:
                ValidationResultMutable vr = new ValidationResultMutable();
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("validation.error.operation.soap"));
                return vr;
            }
        }
        return ValidationResult.OK;
    }

    public void beforeOutputOperation() {
        if (isApiSOAP()) {
            outputOperation.setPossibleValues(syncLead, syncMultipleLeads);
            switch (outputOperation.getValue()) {
            case syncLead:
            case syncMultipleLeads:
                break;
            default:
                outputOperation.setValue(syncLead);
            }
        } else {
            outputOperation.setPossibleValues(OutputOperation.values());
        }
    }

    public void beforeMappingInput() {
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

    public void afterOutputOperation() {
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
        Schema s = null;
        if (isApiSOAP()) {
            // ensure we have at least one schema set
            s = MarketoConstants.getSOAPOutputSchemaForSyncLead();
            switch (outputOperation.getValue()) {
            case syncLead:
                s = MarketoConstants.getSOAPOutputSchemaForSyncLead();
                break;
            case syncMultipleLeads:
                s = MarketoConstants.getSOAPOutputSchemaForSyncMultipleLeads();
                break;
            }
        } else {
            // ensure we have at least one schema set
            s = MarketoConstants.getRESTOutputSchemaForSyncLead();
            switch (outputOperation.getValue()) {
            case syncLead:
                s = MarketoConstants.getRESTOutputSchemaForSyncLead();
                break;
            case syncMultipleLeads:
                s = MarketoConstants.getRESTOutputSchemaForSyncMultipleLeads();
                break;
            case deleteLeads:
                s = MarketoConstants.getDeleteLeadsSchema();
                break;
            case syncCustomObjects:
            case deleteCustomObjects:
                s = MarketoConstants.getCustomObjectSyncSchema();
                break;
            }
        }
        schemaInput.schema.setValue(s);
        updateOutputSchemas();
    }

    public void updateOutputSchemas() {
        Schema inputSchema = schemaInput.schema.getValue();
        inputSchema.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        // batch processing workaround - studio can't handle feedbacks correctly on close call.
        // so we set an empty schema for now.
        // seems that it may be resolved : https://jira.talendforge.org/browse/TDI-38603
        if ((outputOperation.getValue().equals(deleteLeads) && deleteLeadsInBatch.getValue())
                || (outputOperation.getValue().equals(syncMultipleLeads) && batchSize.getValue() > 1)) {
            schemaFlow.schema.setValue(inputSchema);
            schemaReject.schema.setValue(MarketoConstants.getEmptySchema());
            return;
        }
        final List<Field> flowFields = new ArrayList<Field>();
        final List<Field> rejectFields = new ArrayList<Field>();
        Field f;
        // all other cases
        boolean isCustomObject = (outputOperation.getValue().equals(syncCustomObjects)
                || outputOperation.getValue().equals(deleteCustomObjects));
        //
        if (inputSchema.getField(MarketoConstants.FIELD_STATUS) == null) {
            f = new Field(MarketoConstants.FIELD_STATUS, Schema.create(Type.STRING), null, (Object) null);
            f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
            f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
            flowFields.add(f);
        }
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
        if (inputSchema.getField(MarketoConstants.FIELD_STATUS) == null) {
            f = new Field(MarketoConstants.FIELD_STATUS, Schema.create(Type.STRING), null, (Object) null);
            f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
            f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
            rejectFields.add(f);
        }
        if (inputSchema.getField(MarketoConstants.FIELD_ERROR_MSG) == null) {
            f = new Field(MarketoConstants.FIELD_ERROR_MSG, Schema.create(Schema.Type.STRING), null, (Object) null);
            f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
            f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
            rejectFields.add(f);
        }
        Schema flowSchema = MarketoUtils.newSchema(inputSchema, "schemaFlow", flowFields);
        Schema rejectSchema = MarketoUtils.newSchema(inputSchema, "schemaReject", rejectFields);
        schemaFlow.schema.setValue(flowSchema);
        schemaReject.schema.setValue(rejectSchema);
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean migrated;
        try {
            migrated = super.postDeserialize(version, setup, persistent);
        } catch (ClassCastException cce) {
            migrated = super.postDeserialize(version, setup, false); // don't initLayout
        }
        checkForInvalidStoredProperties();

        return migrated;
    }

    /*
     * Some jobs were corrupted between 6.4 and 6.5 (Class name changes). This fixes thoses jobs in error with a
     * ClassCastException : LinkedHashMap cannot be cast to Enum.
     */
    private void checkForInvalidStoredProperties() {
        outputOperation = checkForInvalidStoredEnumProperty(outputOperation, OutputOperation.class);
        customObjectSyncAction = checkForInvalidStoredEnumProperty(customObjectSyncAction, CustomObjectSyncAction.class);
    }

}
