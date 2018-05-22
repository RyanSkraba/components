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
package org.talend.components.snowflake;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.daikon.definition.Definition;

/**
 * Unit tests for {@link SnowflakeFamilyDefinition} class
 */
public class SnowflakeFamilyDefinitionTest {

    private DefinitionRegistry definitionRegistry = new DefinitionRegistry();

    @SuppressWarnings("unchecked")
    @Test
    public void testCreationAndRegistrationOfSnowflakeFamily() {
        SnowflakeFamilyDefinition snowflakeFamilyDefinition = new SnowflakeFamilyDefinition();
        snowflakeFamilyDefinition.install(definitionRegistry);

        Assert.assertEquals(((List<Definition<ComponentProperties>>) snowflakeFamilyDefinition.getDefinitions()).size(),
                definitionRegistry.getDefinitions().size());
    }
}
