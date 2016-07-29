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
package org.talend.components.datastewardship;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;

/**
 * TDS Campaign {@link Properties}
 */
public class TdsCampaignProperties extends PropertiesImpl {

    /**
     * Campaign name
     */
    public Property<String> campaignName = newProperty("campaignName").setRequired(); //$NON-NLS-1$

    /**
     * Campaign label
     */
    public Property<String> campaignLabel = newProperty("campaignLabel"); //$NON-NLS-1$

    /**
     * Campaign type
     */
    public EnumProperty<CampaignType> campaignType = new EnumProperty<CampaignType>(CampaignType.class, "campaignType"); //$NON-NLS-1$

    /**
     * Campaign structure
     */
    public Property<String> campaignStructure = newProperty("campaignStructure"); //$NON-NLS-1$

    /**
     * @param name
     */
    public TdsCampaignProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(campaignName);
        mainForm.addColumn(campaignType);
        /*
        mainForm.addRow(campaignLabel);
        mainForm.addRow(campaignStructure);
        */
    }

}
