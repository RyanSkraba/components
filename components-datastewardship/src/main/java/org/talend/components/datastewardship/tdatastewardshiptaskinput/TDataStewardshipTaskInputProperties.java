// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship.tdatastewardshiptaskinput;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.datastewardship.TdsCampaignProperties;
import org.talend.components.datastewardship.TdsSearchCriteriaProperties;
import org.talend.components.datastewardship.common.CampaignDetail;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.common.TdsConstants;
import org.talend.components.datastewardship.common.TdsUtils;
import org.talend.components.datastewardship.common.CampaignDetail.RecordField;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.di.DiSchemaConstants;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

/**
 * {@link Properties} for Data Stewardship Task input component.
 */
public class TDataStewardshipTaskInputProperties extends TdsCampaignProperties {

    /**
     * Search Criteria
     */
    public TdsSearchCriteriaProperties searchCriteria = new TdsSearchCriteriaProperties("searchCriteria"); //$NON-NLS-1$

    /**
     * Retrieve golden record only
     */
    public Property<Boolean> goldenOnly = newBoolean("goldenOnly", true); //$NON-NLS-1$

    /**
     * batch size
     */
    public Property<Integer> batchSize = newInteger("batchSize", 50); //$NON-NLS-1$

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TDataStewardshipTaskInputProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(searchCriteria.getForm(Form.MAIN));
        mainForm.addRow(goldenOnly);
        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(batchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("deprecation")
    public void afterGoldenOnly() {
        if (CampaignType.MERGING == campaignType.getValue()) { // only affect MERGING task
            Schema oldSchema = schema.schema.getValue();
            List<Field> oldFields = oldSchema.getFields();
            // Initialize schema
            Schema newSchema = Schema.createRecord(oldSchema.getName(), oldSchema.getDoc(), oldSchema.getNamespace(), // $NON-NLS-1$
                    oldSchema.isError());
            for (Map.Entry<String, Object> entry : oldSchema.getObjectProps().entrySet()) {
                newSchema.addProp(entry.getKey(), entry.getValue());
            }
            // Initialize fields
            List<Field> newFields = new ArrayList<Field>();
            if (goldenOnly.getValue()) {// Retrieve golden record only
                for (Field oldField : oldFields) {
                    if (!oldField.name().equals(TdsConstants.META_MASTER) && !oldField.name().equals(TdsConstants.META_SOURCE)) {
                        Field newField = new Schema.Field(oldField.name(), oldField.schema(), oldField.doc(),
                                oldField.defaultValue());
                        for (Map.Entry<String, Object> entry : oldField.getObjectProps().entrySet()) {
                            newField.addProp(entry.getKey(), entry.getValue());
                        }
                        newFields.add(newField);
                    }
                }
            } else {
                for (Field oldField : oldFields) {
                    Field newField = new Schema.Field(oldField.name(), oldField.schema(), oldField.doc(),
                            oldField.defaultValue());
                    for (Map.Entry<String, Object> entry : oldField.getObjectProps().entrySet()) {
                        newField.addProp(entry.getKey(), entry.getValue());
                    }
                    newFields.add(newField);
                }

                Field masterField = new Schema.Field(TdsConstants.META_MASTER, AvroUtils._boolean(), null, null);
                masterField.addProp(SchemaConstants.TALEND_IS_LOCKED, Boolean.TRUE.toString());
                masterField.addProp(DiSchemaConstants.TALEND6_COLUMN_IS_NULLABLE, false);
                newFields.add(masterField);

                Field sourceField = new Schema.Field(TdsConstants.META_SOURCE, AvroUtils._string(), null, null);
                sourceField.addProp(SchemaConstants.TALEND_IS_LOCKED, Boolean.TRUE.toString());
                sourceField.addProp(DiSchemaConstants.TALEND6_COLUMN_IS_NULLABLE, false);
                newFields.add(sourceField);
            }
            newSchema.setFields(newFields);
            // Update schema
            schema.schema.setValue(newSchema);
        }
    }

    @Override
    protected RecordField[] getSchemaFields(CampaignDetail campaign) {
        List<RecordField> allFields = new ArrayList<RecordField>();
        allFields.addAll(campaign.getFields());
        allFields.addAll(TdsUtils.getMetadataFieldsForInput(goldenOnly.getValue()));
        return allFields.toArray(new RecordField[allFields.size()]);
    }

    @Override
    protected void setupRelatedProperties(CampaignDetail campaign) {
        searchCriteria.taskState.setPossibleValues(campaign.getStates());
        searchCriteria.taskState.setValue(campaign.getStates().get(0));
    }
}
