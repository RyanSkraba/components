// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.google.drive;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;

public class GoogleDriveFamilyDefinitionTest extends GoogleDriveTestBase {

    public static final String GOOGLE_DRIVE = "GoogleDrive";

    GoogleDriveFamilyDefinition def;

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    @Before
    public void setUp() throws Exception {
        def = new GoogleDriveFamilyDefinition();
    }

    @Test
    public void testGoogleDriveFamilyDefinition() {
        assertNotNull(getDefinitionRegistry());
        assertEquals(GOOGLE_DRIVE, new GoogleDriveFamilyDefinition().getName());
        assertEquals(9, definitionRegistry.getDefinitions().size());
    }

    @Test
    public void testBasic() {
        assertThat(def.getName(), is(GOOGLE_DRIVE));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("tGoogleDriveConnection"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("tGoogleDriveCopy"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("tGoogleDriveCreate"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("tGoogleDriveDelete"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("tGoogleDriveGet"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("tGoogleDrivePut"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("tGoogleDriveList"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("GoogleDrive"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("GoogleDrive.edit"))));
    }

    /**
     * The component family for this component is also the {@link ComponentInstaller}.
     */
    @Test
    public void testInstall() {
        ((ComponentInstaller) def).install(ctx);
        Mockito.verify(ctx, times(1)).registerComponentFamilyDefinition(any(ComponentFamilyDefinition.class));
    }

}
