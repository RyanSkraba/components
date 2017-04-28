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

package org.talend.components.simplefileio.s3;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIODatasetDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link SimpleFileIODatasetDefinition}.
 */
public class S3DatasetDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatasetDefinition<?> def = new S3DatasetDefinition();

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("S3Dataset"));
        assertThat((Object) def.getPropertiesClass(), is(equalTo((Object) S3DatasetProperties.class)));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(null);
        assertThat(runtimeInfo.getRuntimeClassName(), is("org.talend.components.simplefileio.runtime.s3.S3DatasetRuntime"));
        // The integration module tests things that aren't available in the RuntimeInfo module until after it is
        // installed in the local maven repository.
    }
}
