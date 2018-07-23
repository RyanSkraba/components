// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.definition.DefinitionImageType;

public class MarketoConnectionWizardTest {

    MarketoConnectionWizard wiz;

    MarketoConnectionWizardDefinition def;

    String repoLoc = "___DRI";

    @Before
    public void setUp() throws Exception {
        def = new MarketoConnectionWizardDefinition();
        wiz = new MarketoConnectionWizard(def, repoLoc);
    }

    @Test
    public void testSetupProperties() throws Exception {
        TMarketoConnectionProperties p = new TMarketoConnectionProperties("test");
        p.setupProperties();
        wiz.setupProperties(p);
        assertEquals(p, wiz.connProperties);
        assertEquals(p, wiz.coProps.connection);
    }

    @Test
    public void testRepositoryLocation() throws Exception {
        assertEquals(repoLoc, wiz.getRepositoryLocation());
    }

    @Test
    public void testTopLevel() throws Exception {
        assertTrue(wiz.getDefinition().isTopLevel());
        assertFalse(new MarketoConnectionEditWizardDefinition().isTopLevel());
    }

    @Test
    public void testWizardDefinition() throws Exception {
        assertEquals("marketo", def.getName());
        assertEquals("marketo.edit", new MarketoConnectionEditWizardDefinition().getName());
        assertNull(def.getIconKey());
        assertEquals("connectionWizardIcon.png", def.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        assertEquals("marketoWizardBanner.png", def.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66));
        assertEquals("connectionWizardIcon.png", def.getPngImagePath(null));
        assertNull(def.getImagePath(DefinitionImageType.SVG_ICON));
        assertNull(def.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        assertTrue(def.supportsProperties(TMarketoConnectionProperties.class));
    }

    @Test
    public void testWizardDefinitionCreateWizard() throws Exception {
        MarketoConnectionWizard w = (MarketoConnectionWizard) def.createWizard(repoLoc);
        assertNotNull(w);
        w = (MarketoConnectionWizard) def.createWizard(new TMarketoConnectionProperties("test"), repoLoc);
        assertNotNull(w);
    }
}
