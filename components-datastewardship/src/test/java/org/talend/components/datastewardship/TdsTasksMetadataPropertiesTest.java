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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TdsTasksMetadataProperties} class
 */
public class TdsTasksMetadataPropertiesTest {

    /**
     * Checks {@link TdsConnectionProperties#setupLayout()} creates Main form, which contain 3 widgets and checks
     * widgets names <br>
     */
    @Test
    public void testSetupLayout() {
        TdsTasksMetadataProperties properties = new TdsTasksMetadataProperties("root"); //$NON-NLS-1$
        properties.init();
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(mainWidgets, hasSize(5));
        Widget taskPriorityWidget = main.getWidget("taskPriority"); //$NON-NLS-1$
        assertThat(taskPriorityWidget, notNullValue());
        Widget taskTagsWidget = main.getWidget("taskTags"); //$NON-NLS-1$
        assertThat(taskTagsWidget, notNullValue());
        Widget taskStateWidget = main.getWidget("taskState"); //$NON-NLS-1$
        assertThat(taskStateWidget, notNullValue());
        Widget taskAssigneeWidget = main.getWidget("taskAssignee"); //$NON-NLS-1$
        assertThat(taskAssigneeWidget, notNullValue());
        Widget taskCommentWidget = main.getWidget("taskComment"); //$NON-NLS-1$
        assertThat(taskCommentWidget, notNullValue());
    }

}
