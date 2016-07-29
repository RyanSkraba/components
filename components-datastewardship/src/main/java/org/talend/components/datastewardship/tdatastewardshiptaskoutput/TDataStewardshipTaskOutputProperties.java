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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.datastewardship.TdsCampaignProperties;
import org.talend.components.datastewardship.TdsProperties;
import org.talend.components.datastewardship.TdsTasksMetadataProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;

/**
 * {@link Properties} for Data Stewardship Task output component.
 */
public class TDataStewardshipTaskOutputProperties extends TdsProperties {

    /**
     * Campaign
     */
    public TdsCampaignProperties campaign = new TdsCampaignProperties("campaign"); //$NON-NLS-1$

    /**
     * Tasks metadata
     */
    public TdsTasksMetadataProperties tasksMetadata = new TdsTasksMetadataProperties("tasksMetadata"); //$NON-NLS-1$

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(campaign.getForm(Form.MAIN));
        //mainForm.addRow(tasksMetadata.getForm(Form.MAIN));
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
            return Collections.emptySet();
        }
        return Collections.singleton(MAIN_CONNECTOR);
    }

    private boolean isRequiredFieldRight() {
        return !isEmpty(connection.url.getStringValue()) && !isEmpty(connection.username.getStringValue())
                && !isEmpty(connection.password.getStringValue()) && !isEmpty(campaign.campaignName.getStringValue());
    }

}
