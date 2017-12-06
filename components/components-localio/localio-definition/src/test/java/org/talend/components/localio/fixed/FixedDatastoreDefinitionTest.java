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
package org.talend.components.localio.fixed;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link FixedDatastoreDefinition}.
 */
public class FixedDatastoreDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatastoreDefinition<?> def = new FixedDatastoreDefinition();

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("FixedDatastore"));
        assertThat((Object) def.getPropertiesClass(), is(equalTo((Object) FixedDatastoreProperties.class)));
        assertThat(def.getInputCompDefinitionName(), is("FixedInput"));
        assertThat(def.getOutputCompDefinitionName(), is("DevNullOutput"));
        assertThat(def.getImagePath(DefinitionImageType.PALETTE_ICON_32X32), nullValue());
        assertThat(def.getImagePath(DefinitionImageType.SVG_ICON), nullValue());
        assertThat(def.getIconKey(), is("streams"));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(null);
        assertThat(runtimeInfo.getRuntimeClassName(), is("org.talend.components.localio.runtime.fixed.FixedDatastoreRuntime"));
// Other runtime information is not available until the runtime module is built and installed.
    }
}
