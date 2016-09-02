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
package org.talend.components.datastewardship.tdatastewardshiptaskoutput;

import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.datastewardship.TdsAdvancedMappingsProperties;
import org.talend.components.datastewardship.TdsCampaignProperties;
import org.talend.components.datastewardship.TdsTasksMetadataProperties;
import org.talend.components.datastewardship.common.CampaignDetail;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.common.TdsConstants;
import org.talend.components.datastewardship.common.TdsUtils;
import org.talend.components.datastewardship.common.CampaignDetail.RecordField;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

/**
 * {@link Properties} for Data Stewardship Task output component.
 */
public class TDataStewardshipTaskOutputProperties extends TdsCampaignProperties {

    /**
     * Tasks metadata
     */
    public TdsTasksMetadataProperties tasksMetadata = new TdsTasksMetadataProperties("tasksMetadata"); //$NON-NLS-1$

    /**
     * Advanced Mappings Properties
     */
    public TdsAdvancedMappingsProperties advancedMappings = new TdsAdvancedMappingsProperties("advancedMappings"); //$NON-NLS-1$

    /**
     * batch size
     */
    public Property<Integer> batchSize = newInteger("batchSize", 50); //$NON-NLS-1$

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TDataStewardshipTaskOutputProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        this.setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                List<String> fieldNames = getFieldNames(schema.schema);
                advancedMappings.groupIdColumn.setPossibleValues(fieldNames);
                advancedMappings.sourceColumn.setPossibleValues(fieldNames);
                advancedMappings.masterColumn.setPossibleValues(fieldNames);
                advancedMappings.scoreColumn.setPossibleValues(fieldNames);
            }

        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(tasksMetadata.getForm(Form.MAIN));
        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(advancedMappings.getForm(Form.ADVANCED));
        advancedForm.addRow(batchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.ADVANCED)) {
            CampaignType type = campaignType.getValue();
            if (type != null) {
                switch (type) {
                case MERGING:
                    form.getWidget(advancedMappings.getName()).setHidden(false);
                    break;
                default:
                    form.getWidget(advancedMappings.getName()).setHidden(true);
                    break;
                }
            }
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.emptySet();
        }
        return Collections.singleton(MAIN_CONNECTOR);
    }

    @SuppressWarnings("rawtypes")
    protected List<String> getFieldNames(Property schema) {
        Schema s = (Schema) schema.getValue();
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : s.getFields()) {
            if (!Boolean.valueOf(f.getProp(SchemaConstants.TALEND_IS_LOCKED))) {
                fieldNames.add(f.name());
            }
        }
        return fieldNames;
    }

    @Override
    protected RecordField[] getSchemaFields(CampaignDetail campaign) {
        List<RecordField> allFields = new ArrayList<RecordField>();
        allFields.addAll(campaign.getFields());
        if (campaign.isMerging()) {
            allFields.addAll(TdsUtils.getMetadataFieldsForOutputMerging());
        }
        return allFields.toArray(new RecordField[allFields.size()]);
    }

    @Override
    protected void setupRelatedProperties(CampaignDetail campaign) {
        tasksMetadata.taskState.setPossibleValues(campaign.getStates());
        tasksMetadata.taskState.setValue(campaign.getStates().get(0));
        if (campaign.isMerging()) {
            advancedMappings.groupIdColumn.setValue(TdsConstants.META_GID);
            advancedMappings.masterColumn.setValue(TdsConstants.META_MASTER);
            advancedMappings.sourceColumn.setValue(TdsConstants.META_SOURCE);
            advancedMappings.scoreColumn.setValue(TdsConstants.META_SCORE);
        }
    }
}
