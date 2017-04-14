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

package org.talend.components.jdbc.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.jdbc.dataprep.JDBCInputDefinition;
import org.talend.components.jdbc.dataprep.JDBCInputProperties;
import org.talend.components.jdbc.datastream.JDBCOutputDefinition;
import org.talend.components.jdbc.datastream.JDBCOutputProperties;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class JdbcInputOutputRuntimeTest {

    @Test
    public void testBasic() {
        JDBCInputProperties inputProperties = new JDBCInputProperties("input");
        inputProperties.init();
        inputProperties.setDatasetProperties(JdbcDatasetRuntimeTest.createDatasetProperties());

        JDBCInputDefinition inputDefinition = new JDBCInputDefinition();
        RuntimeInfo inputRI = inputDefinition.getRuntimeInfo(ExecutionEngine.BEAM, inputProperties, ConnectorTopology.OUTGOING);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(inputRI, getClass().getClassLoader())) {
            assertThat(si.getInstance().getClass().getCanonicalName(), is(JDBCInputDefinition.BEAM_RUNTIME));
        }
        inputRI = inputDefinition.getRuntimeInfo(ExecutionEngine.DI, inputProperties, ConnectorTopology.OUTGOING);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(inputRI, getClass().getClassLoader())) {
            assertThat(si.getInstance().getClass().getCanonicalName(), is(JDBCInputDefinition.DI_RUNTIME));
        }

        JDBCOutputProperties outputProperties = new JDBCOutputProperties("output");
        outputProperties.init();
        outputProperties.setDatasetProperties(JdbcDatasetRuntimeTest.createDatasetProperties());

        JDBCOutputDefinition outputDefinition = new JDBCOutputDefinition();
        RuntimeInfo outputRI = outputDefinition.getRuntimeInfo(ExecutionEngine.BEAM, outputProperties,
                ConnectorTopology.INCOMING);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(outputRI, getClass().getClassLoader())) {
            assertThat(si.getInstance().getClass().getCanonicalName(), is(JDBCOutputDefinition.BEAM_RUNTIME));
        }
    }
}
