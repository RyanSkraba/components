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
package org.talend.components.elasticsearch.integration;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.elasticsearch.ElasticsearchDatastoreDefinition;
import org.talend.components.elasticsearch.ElasticsearchDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit tests for {@link ElasticsearchDatastoreDefinition} runtimes loaded dynamically.
 */
public class ElasticsearchDatastoreTestIT {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ElasticsearchDatastoreDefinition def = new ElasticsearchDatastoreDefinition();

    /**
     * @return the properties for this datastore, fully initialized with the default values and credentials
     * from the System environment.
     */
    public static ElasticsearchDatastoreProperties createDatastoreProperties() {
        // Configure the dataset.
        ElasticsearchDatastoreProperties datastoreProps = new ElasticsearchDatastoreProperties(null);
        datastoreProps.init();
        datastoreProps.nodes.setValue(System.getProperty("es.hosts"));
        return datastoreProps;
    }

    @Test
    public void testBasic() throws Exception {
        ElasticsearchDatastoreProperties props = createDatastoreProperties();

        RuntimeInfo ri = def.getRuntimeInfo(props);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {

            DatastoreRuntime runtime = (DatastoreRuntime) si.getInstance();
            runtime.initialize(null, props);
            assertThat(runtime, not(nullValue()));

            Iterator iterator = runtime.doHealthChecks(null).iterator();
            assertTrue(iterator.hasNext());
            assertEquals(ValidationResult.OK, iterator.next());
        }
    }
}
