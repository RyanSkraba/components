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
package org.talend.components.marklogic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.marklogic.data.MarkLogicDatasetDefinition;
import org.talend.components.marklogic.data.MarkLogicDatastoreDefinition;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadDefinition;
import org.talend.components.marklogic.tmarklogicclose.MarkLogicCloseDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputDefinition;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputDefinition;
import org.talend.components.marklogic.wizard.MarkLogicWizardDefinition;
import org.talend.daikon.definition.Definition;

public class MarkLogicFamilyDefinitionTest {

    private MarkLogicFamilyDefinition familyDefinition = null;

    @Before
    public void init() {
        familyDefinition = new MarkLogicFamilyDefinition();
    }

    @Test
    public void testAllComponentsDefinitionsCreated() {
        List<Class> expectedDefinitions = new ArrayList<>();
        expectedDefinitions.add(MarkLogicInputDefinition.class);
        expectedDefinitions.add(MarkLogicOutputDefinition.class);
        expectedDefinitions.add(MarkLogicConnectionDefinition.class);
        expectedDefinitions.add(MarkLogicCloseDefinition.class);
        expectedDefinitions.add(MarkLogicBulkLoadDefinition.class);
        expectedDefinitions.add(MarkLogicWizardDefinition.class);
        expectedDefinitions.add(MarkLogicDatasetDefinition.class);
        expectedDefinitions.add(MarkLogicDatastoreDefinition.class);

        List<Class> actualDefinitionsNames = new ArrayList<>();

        for (Definition d : familyDefinition.getDefinitions()) {
            actualDefinitionsNames.add(d.getClass());
        }

        assertEquals(expectedDefinitions, actualDefinitionsNames);
    }

    @Test
    public void isFamilyInstalled() {
        ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

        familyDefinition.install(ctx);

        Mockito.verify(ctx).registerComponentFamilyDefinition(familyDefinition);
    }
}
