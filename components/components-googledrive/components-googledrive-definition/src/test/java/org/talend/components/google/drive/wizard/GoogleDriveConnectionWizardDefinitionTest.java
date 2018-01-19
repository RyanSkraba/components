package org.talend.components.google.drive.wizard;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;
import static org.talend.components.google.drive.wizard.GoogleDriveConnectionWizardDefinition.CONNECTION_WIZARD_ICON_PNG;
import static org.talend.components.google.drive.wizard.GoogleDriveConnectionWizardDefinition.WIZARD_BANNER_PNG;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.definition.DefinitionImageType;

public class GoogleDriveConnectionWizardDefinitionTest {

    @Test
    public void testSupportsProperties1() throws Exception {
    }

    public static final String REPOSITORY_LOCATION = "/fake/location";

    private GoogleDriveConnectionWizardDefinition definition;

    @Before
    public void setUp() throws Exception {
        definition = new GoogleDriveConnectionWizardDefinition();
    }

    @Test
    public void testCreateWizard() throws Exception {
        GoogleDriveConnectionWizard wizard = (GoogleDriveConnectionWizard) definition.createWizard(REPOSITORY_LOCATION);
        assertNotNull(wizard);
        assertThat(wizard, isA(GoogleDriveConnectionWizard.class));
        assertEquals(definition, wizard.getDefinition());
        assertThat(wizard.getRepositoryLocation(), is(REPOSITORY_LOCATION));
    }

    @Test
    public void testCreateWizardWithProperties() throws Exception {
        GoogleDriveConnectionProperties connection = new GoogleDriveConnectionProperties("test");
        GoogleDriveConnectionWizard wizard = (GoogleDriveConnectionWizard) definition.createWizard(connection,
                REPOSITORY_LOCATION);
        assertNotNull(wizard);
        assertThat(wizard, isA(GoogleDriveConnectionWizard.class));
        assertEquals(definition, wizard.getDefinition());
        assertThat(wizard.getRepositoryLocation(), is(REPOSITORY_LOCATION));
    }

    @Test
    public void testSupportsProperties() throws Exception {
        assertTrue(definition.supportsProperties(GoogleDriveConnectionProperties.class));
    }

    @Test
    public void testGetPngImagePath() throws Exception {
        assertThat(definition.getPngImagePath(DefinitionImageType.TREE_ICON_16X16), is(CONNECTION_WIZARD_ICON_PNG));
        assertThat(definition.getPngImagePath(DefinitionImageType.WIZARD_BANNER_75X66), is(WIZARD_BANNER_PNG));
        assertNull(definition.getPngImagePath(DefinitionImageType.SVG_ICON));
        assertNull(definition.getPngImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        //
        assertThat(definition.getPngImagePath(WizardImageType.TREE_ICON_16X16), is(CONNECTION_WIZARD_ICON_PNG));
        assertThat(definition.getPngImagePath(WizardImageType.WIZARD_BANNER_75X66), is(WIZARD_BANNER_PNG));
    }

    @Test
    public void testGetImagePath() throws Exception {
        assertThat(definition.getImagePath(DefinitionImageType.TREE_ICON_16X16), is(CONNECTION_WIZARD_ICON_PNG));
        assertThat(definition.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66), is(WIZARD_BANNER_PNG));
        assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
    }

    @Test
    public void testGetIconKey() throws Exception {
        assertNull(definition.getIconKey());
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(definition.getName(), is(GoogleDriveConnectionWizardDefinition.COMPONENT_WIZARD_NAME));
    }

    @Test
    public void testIsTopLevel() throws Exception {
        assertTrue(definition.isTopLevel());
    }

}
