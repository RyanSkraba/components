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
package org.talend.components.snowflake.runtime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.properties.ValidationResult;

public class SnowflakeRowSinkTest {

    private SnowflakeRowSink sink;

    @Before
    public void setup() {
        sink = new SnowflakeRowSink();
    }

    @Test
    public void testInitialize() {
        RuntimeContainer container = Mockito.mock(RuntimeContainer.class);
        TSnowflakeRowProperties properties = new TSnowflakeRowProperties("rowProperties");

        Assert.assertNull(sink.getRowProperties());

        ValidationResult result = sink.initialize(container, properties);

        Assert.assertEquals(ValidationResult.OK, result);
        Assert.assertEquals(properties, sink.getRowProperties());
    }

    @Test
    public void testCreateWriteOperation() {

        Assert.assertTrue(sink.createWriteOperation() instanceof SnowflakeRowWriteOperation);
    }

}
