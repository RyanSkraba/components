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
package org.talend.components.marketo.runtime.data;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.MarketoRuntimeTestBase;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoDatastoreRuntimeTest extends MarketoRuntimeTestBase {

    private MarketoDatastoreRuntime runtime;

    private TMarketoConnectionProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        properties = new TMarketoConnectionProperties("test");
        properties.init();
        properties.setupProperties();
        properties.setupLayout();
        runtime = spy(new MarketoDatastoreRuntime());
    }

    @Test
    public void testInitialize() throws Exception {
        assertEquals(ValidationResult.OK, runtime.initialize(container, properties));
    }

    @Test
    public void testDoHealthChecks() throws Exception {
        when(runtime.getNewSourceOrSink()).thenReturn(sourceOrSink);
        runtime.initialize(container, properties);
        runtime.doHealthChecks(container).forEach(r -> assertEquals(Result.OK, r.getStatus()));
        when(sourceOrSink.getClientService(container)).thenThrow(new IOException("Failing"));
        runtime.doHealthChecks(container).forEach(r -> assertEquals(Result.ERROR, r.getStatus()));
    }
}
