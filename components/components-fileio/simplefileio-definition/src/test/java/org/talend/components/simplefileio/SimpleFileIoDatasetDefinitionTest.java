// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.simplefileio;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIoDatasetDefinition}.
 */
public class SimpleFileIoDatasetDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatasetDefinition<?> def = new SimpleFileIoDatasetDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("SimpleFileIoDataset"));
        assertThat((Object) def.getPropertiesClass(), is(equalTo((Object) SimpleFileIoDatasetProperties.class)));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(null, null);
        assertEquals("org.talend.components.simplefileio.runtime.SimpleFileIoDatasetRuntime", runtimeInfo.getRuntimeClassName());
    }
}