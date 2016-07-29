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
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.StringProperty;

/**
 * Tasks metadata {@link Properties}
 */
public class TdsTasksMetadataProperties extends PropertiesImpl {

    /**
     * Task Priority
     */
    public EnumProperty<TaskPriority> taskPriority = new EnumProperty<TaskPriority>(TaskPriority.class, "taskPriority"); //$NON-NLS-1$

    /**
     * Task Tags
     */
    public StringProperty taskTags = new StringProperty("taskTags"); //$NON-NLS-1$

    /**
     * Task State
     */
    public StringProperty taskState = new StringProperty("taskState"); //$NON-NLS-1$

    /**
     * Task Assignee
     */
    public StringProperty taskAssignee = new StringProperty("taskAssignee"); //$NON-NLS-1$

    /**
     * Task Comment
     */
    public StringProperty taskComment = new StringProperty("taskComment"); //$NON-NLS-1$

    /**
     * Constructor sets properties name
     * 
     * @param name properties name
     */
    public TdsTasksMetadataProperties(String name) {
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
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(taskPriority);
        mainForm.addColumn(taskTags);
        mainForm.addRow(taskState);
        mainForm.addColumn(taskAssignee);
        mainForm.addRow(taskComment);
    }
}
