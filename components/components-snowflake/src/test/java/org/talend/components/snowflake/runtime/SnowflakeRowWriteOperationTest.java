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
package org.talend.components.snowflake.runtime;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;

public class SnowflakeRowWriteOperationTest {

    private SnowflakeRowWriteOperation operation;

    private SnowflakeRowSink sink = new SnowflakeRowSink();

    @Before
    public void setup() {

        operation = new SnowflakeRowWriteOperation(sink);
    }

    @Test
    public void testFinalize() {
        Result result = new Result();
        result.totalCount = 1;
        result.successCount = 1;

        Map<String, Object> resultMap = operation.finalize(Collections.singletonList(result), null);

        Assert.assertEquals(result.totalCount, (int) resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        Assert.assertEquals(result.successCount, (int) resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        Assert.assertEquals(result.rejectCount, (int) resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));
    }

    @Test
    public void testCreateWriter() {
        TSnowflakeRowProperties properties = new TSnowflakeRowProperties("rowProperties");
        sink.initialize(null, properties);

        Writer<Result> writer = operation.createWriter(null);

        Assert.assertTrue(writer instanceof SnowflakeRowWriter);
    }

    @Test
    public void testGetSink() {
        Assert.assertEquals(sink, operation.getSink());
    }
}
