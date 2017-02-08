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

package org.talend.components.localio;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;

/**
 * Unit tests for {@link LocalIOComponentFamilyDefinition}.
 */
public class LocalIOComponentFamilyDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ComponentFamilyDefinition def = new LocalIOComponentFamilyDefinition();

    ComponentInstaller.ComponentFrameworkContext ctx = Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("LocalIO"));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("FixedFlowInput"))));
        assertThat(def.getDefinitions(), (Matcher) hasItem(hasProperty("name", is("RowGenerator"))));
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
