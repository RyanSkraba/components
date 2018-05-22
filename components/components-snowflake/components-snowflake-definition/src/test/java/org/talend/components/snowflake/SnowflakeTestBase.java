// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake;

import org.junit.Before;
import org.mockito.Mockito;
import org.talend.components.common.test.TestFixture;
import org.talend.daikon.sandbox.SandboxedInstance;

public class SnowflakeTestBase {

    public static class MockRuntimeSourceOrSinkTestFixture implements TestFixture, AutoCloseable {

        public SnowflakeRuntimeSourceOrSink runtimeSourceOrSink;

        @Before
        public void setUp() throws Exception {
            SnowflakeDefinition.SandboxedInstanceProvider sandboxedInstanceProvider = Mockito
                    .mock(SnowflakeDefinition.SandboxedInstanceProvider.class);
            SnowflakeDefinition.setSandboxedInstanceProvider(sandboxedInstanceProvider);
            SandboxedInstance sandboxedInstance = Mockito.mock(SandboxedInstance.class);
            Mockito.when(sandboxedInstanceProvider.getSandboxedInstance(Mockito.anyString(), Mockito.anyBoolean()))
                    .thenReturn(sandboxedInstance);
            runtimeSourceOrSink = Mockito.mock(SnowflakeRuntimeSourceOrSink.class);
            Mockito.when(sandboxedInstance.getInstance()).thenReturn(runtimeSourceOrSink);
        }

        @Override
        public void tearDown() throws Exception {
            SnowflakeDefinition.setSandboxedInstanceProvider(
                    SnowflakeDefinition.SandboxedInstanceProvider.INSTANCE);
        }

        @Override
        public void close() throws Exception {
            tearDown();
        }
    }
}
