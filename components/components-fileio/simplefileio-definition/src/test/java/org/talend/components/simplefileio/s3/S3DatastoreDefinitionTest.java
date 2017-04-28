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
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link S3DatastoreDefinition}.
 */
public class S3DatastoreDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatastoreDefinition<?> def = new S3DatastoreDefinition();

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("S3Datastore"));
        assertThat((Object) def.getPropertiesClass(), equalTo((Object) S3DatastoreProperties.class));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(null);
        assertThat(runtimeInfo.getRuntimeClassName(), is("org.talend.components.simplefileio.runtime.s3.S3DatastoreRuntime"));
        // The integration module tests things that aren't available in the RuntimeInfo module until after it is
        // installed in the local maven repository.
    }
}
