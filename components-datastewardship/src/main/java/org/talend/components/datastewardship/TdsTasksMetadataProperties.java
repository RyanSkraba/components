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

import org.talend.components.datastewardship.common.TdsConstants;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.StringProperty;

/**
 * Tasks metadata {@link Properties}
 */
public class TdsTasksMetadataProperties extends PropertiesImpl {

    /**
     * Task Priority
     */
    public StringProperty taskPriority = new StringProperty("taskPriority"); //$NON-NLS-1$

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
        taskPriority.setPossibleValues(TdsConstants.PRIORITY_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        // mainForm.addRow(Widget.widget(taskPriority).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        // mainForm.addColumn(taskTags);
        mainForm.addRow(Widget.widget(taskState).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(taskAssignee);
        // mainForm.addRow(taskComment);
    }
}
