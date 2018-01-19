package org.talend.components.google.drive.wizard;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.components.google.drive.wizard.GoogleDriveConnectionEditWizardDefinition.COMPONENT_WIZARD_NAME;

import org.junit.Before;
import org.junit.Test;

public class GoogleDriveConnectionEditWizardDefinitionTest {

    GoogleDriveConnectionEditWizardDefinition definition;

    @Before
    public void setUp() throws Exception {
        definition = new GoogleDriveConnectionEditWizardDefinition();
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(definition.getName(), is(COMPONENT_WIZARD_NAME));
    }

    @Test
    public void testIsTopLevel() throws Exception {
        assertFalse(definition.isTopLevel());
    }

}
