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
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link FixedDatasetDefinition}.
 */
public class FixedDatasetDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatasetDefinition<?> def = new FixedDatasetDefinition();

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("FixedDataset"));
        assertThat((Object) def.getPropertiesClass(), is(equalTo((Object) FixedDatasetProperties.class)));
        assertThat(def.getImagePath(DefinitionImageType.PALETTE_ICON_32X32), nullValue());
        assertThat(def.getImagePath(DefinitionImageType.SVG_ICON), nullValue());
        assertThat(def.getIconKey(), is("file-source-o"));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(null);
        assertThat(runtimeInfo.getRuntimeClassName(), is("org.talend.components.localio.runtime.fixed.FixedDatasetRuntime"));
        // Other runtime information is not available until the runtime module is built and installed.
    }
}
