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
package org.talend.components.marketo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.definition.Definition;

public class MarketoFamilyDefinitionTest extends MarketoTestBase {

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoFamilyDefinitionTest.class);

    /**
     * Test method for {@link org.talend.components.marketo.MarketoFamilyDefinition#MarketoFamilyDefinition()}.
     */
    @Test
    public final void testMarketoFamilyDefinition() {
        assertNotNull(getDefinitionRegistry());
        assertEquals("Marketo", new MarketoFamilyDefinition().getName());
        assertEquals(7, testComponentRegistry.getDefinitions().size());
    }

    @Test
    public void testInstall() throws Exception {
        final MarketoFamilyDefinition def = new MarketoFamilyDefinition();
        ComponentInstaller.ComponentFrameworkContext ctx = new ComponentInstaller.ComponentFrameworkContext() {

            @Override
            public void registerComponentFamilyDefinition(ComponentFamilyDefinition def) {
                LOG.debug("def = [" + def + "]");
                assertEquals("Marketo", def.getName());
            }

            @Override
            public void registerDefinition(Iterable<? extends Definition> defs) {
                LOG.debug("defs = [" + defs + "]");
                assertNull(defs);
            }

            @Override
            public void registerComponentWizardDefinition(Iterable<? extends ComponentWizardDefinition> defs) {
                LOG.debug("defs = [" + defs + "]");
                assertNull(def);
            }
        };
        def.install(ctx);
    }
}
