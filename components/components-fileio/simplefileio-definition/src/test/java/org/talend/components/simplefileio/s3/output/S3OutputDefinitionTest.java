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

package org.talend.components.simplefileio.s3.output;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link S3OutputDefinition}.
 */
public class S3OutputDefinitionTest {

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ComponentDefinition def = new S3OutputDefinition();

    /**
     * Checks the basic attributes of the definition.
     */
    @Test
    public void testBasic() {
        assertThat(def.getName(), is("S3Output"));
        assertThat(def.getFamilies(), arrayContaining("SimpleFileIo"));
        assertThat(def.getPropertiesClass(), is(equalTo((Object) S3OutputProperties.class)));
        assertThat(def.getPngImagePath(ComponentImageType.PALLETE_ICON_32X32), is("S3Output_icon32.png"));
        assertThat(def.getSupportedConnectorTopologies(), contains(ConnectorTopology.INCOMING));
    }

    /**
     * Checks the {@link RuntimeInfo} of the definition.
     */
    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.INCOMING);
        assertThat(runtimeInfo.getRuntimeClassName(), is("org.talend.components.simplefileio.runtime.s3.S3OutputRuntime"));
        // The integration module tests things that aren't available in the RuntimeInfo module until after it is
        // installed in the local maven repository.
    }

    @Test(expected = TalendRuntimeException.class)
    public void testFailRuntimeInfoOutgoing() {
        def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.OUTGOING);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testFailRuntimeInfoIncomingAndOutgoing() {
        def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.INCOMING_AND_OUTGOING);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testFailRuntimeInfoNone() {
        def.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.NONE);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testFailRuntimeInfoDi() {
        def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testFailRuntimeInfoSparkBatch() {
        def.getRuntimeInfo(ExecutionEngine.DI_SPARK_BATCH, null, ConnectorTopology.INCOMING);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testFailRuntimeInfoSparkStreaming() {
        def.getRuntimeInfo(ExecutionEngine.DI_SPARK_STREAMING, null, ConnectorTopology.INCOMING);
    }
}
