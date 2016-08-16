package org.talend.components.datastewardship;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collection;

import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit-tests for {@link TdsMergingTasksMappings} class
 */
public class TdsAdvancedMappingsPropertiesTest {
    /**
     * Checks {@link TdsConnectionProperties#setupLayout()} creates Main form, which contain 4 widgets and checks
     * widgets names <br>
     */
    @Test
    public void testSetupLayout() {
        TdsAdvancedMappingsProperties properties = new TdsAdvancedMappingsProperties("root"); //$NON-NLS-1$
        properties.init();
        properties.setupLayout();

        Form form = properties.getForm(Form.ADVANCED);
        assertThat(form, notNullValue());

        Collection<Widget> mainWidgets = form.getWidgets();
        assertThat(mainWidgets, hasSize(4));
        Widget taskPriorityWidget = form.getWidget("groupIdColumn"); //$NON-NLS-1$
        assertThat(taskPriorityWidget, notNullValue());
        Widget taskTagsWidget = form.getWidget("sourceColumn"); //$NON-NLS-1$
        assertThat(taskTagsWidget, notNullValue());
        Widget taskStateWidget = form.getWidget("masterColumn"); //$NON-NLS-1$
        assertThat(taskStateWidget, notNullValue());
        Widget taskAssigneeWidget = form.getWidget("scoreColumn"); //$NON-NLS-1$
        assertThat(taskAssigneeWidget, notNullValue());
    }
}
