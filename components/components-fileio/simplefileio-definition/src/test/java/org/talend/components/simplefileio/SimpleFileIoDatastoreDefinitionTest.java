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

package org.talend.components.simplefileio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIoDatastoreDefinition}.
 */
public class SimpleFileIoDatastoreDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatastoreDefinition<?> def = new SimpleFileIoDatastoreDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("SimpleFileIoDatastore"));
        assertThat((Object) def.getPropertiesClass(), equalTo((Object) SimpleFileIoDatastoreProperties.class));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(null);
        assertEquals("org.talend.components.simplefileio.runtime.SimpleFileIoDatastoreRuntime", runtimeInfo.getRuntimeClassName());

        // Note that we can't rely on the MavenUrlDependencies being available until after the runtime jar is built, so
        // they are tested there.
        List<URL> dependencies = runtimeInfo.getMavenUrlDependencies();
        assertThat(dependencies, notNullValue());

    }
}
