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
package org.talend.components.localio.integration;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.List;

import org.apache.beam.sdk.testing.TestPipeline;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.localio.rowgenerator.RowGeneratorDefinition;
import org.talend.components.localio.rowgenerator.RowGeneratorProperties;
import org.talend.daikon.avro.SampleSchemas;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit tests for {@link RowGeneratorDefinition} runtimes loaded dynamically.
 *
 * The functionality of the runtime is tested in its own module. This test verifies that the dependencies can be
 * automatically loaded from maven and run without directly depending on the module.
 */
public class SandboxedRowGeneratorRuntimeTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final RowGeneratorDefinition def = new RowGeneratorDefinition();

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    /**
     * @return the properties for this component, fully initialized with the default values.
     */
    public static RowGeneratorProperties createComponentProperties() {
        // Configure the component.
        RowGeneratorProperties componentProps = new RowGeneratorProperties(null);
        componentProps.init();
        return componentProps;
    }

    @Test
    public void testBasic() throws Exception {
        final RowGeneratorProperties props = createComponentProperties();
        props.schemaFlow.schema.setValue(SampleSchemas.recordSimple());
        props.nbRows.setValue(95L);
        props.nbPartitions.setValue(10);

        RuntimeInfo ri = def.getRuntimeInfo(ExecutionEngine.BEAM, props, ConnectorTopology.OUTGOING);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {
            RuntimableRuntime<RowGeneratorProperties> runtime = (RuntimableRuntime<RowGeneratorProperties>) si.getInstance();
            runtime.initialize(null, props);
        }
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo ri = def.getRuntimeInfo(ExecutionEngine.BEAM, createComponentProperties(), ConnectorTopology.OUTGOING);
        List<URL> dependencies = ri.getMavenUrlDependencies();
        // The important part of the test is that no exceptions are thrown while creating the RuntimeInfo.
        assertThat(dependencies, hasSize(greaterThan(40)));
    }

}
