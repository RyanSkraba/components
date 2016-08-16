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

import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.StringProperty;

/**
 * Advanced Mappings Properties {@link Properties}
 */
public class TdsAdvancedMappingsProperties extends PropertiesImpl {

    /**
     * Group ID
     */
    public StringProperty groupIdColumn = new StringProperty("groupIdColumn"); //$NON-NLS-1$

    /**
     * Source
     */
    public StringProperty sourceColumn = new StringProperty("sourceColumn"); //$NON-NLS-1$

    /**
     * Master Indicator
     */
    public StringProperty masterColumn = new StringProperty("masterColumn"); //$NON-NLS-1$

    /**
     * Score
     */
    public StringProperty scoreColumn = new StringProperty("scoreColumn"); //$NON-NLS-1$

    /**
     * Constructor sets properties name
     * 
     * @param name properties name
     */
    public TdsAdvancedMappingsProperties(String name) {
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
        Form form = Form.create(this, Form.ADVANCED);
        form.addRow(groupIdColumn);
        form.addColumn(masterColumn);
        form.addRow(sourceColumn);
        form.addColumn(scoreColumn);
    }
}
