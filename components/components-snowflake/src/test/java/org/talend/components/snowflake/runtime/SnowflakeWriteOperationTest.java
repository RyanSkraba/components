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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;

/**
 * Unit tests for {@link SnowflakeWriteOperation} class
 */
public class SnowflakeWriteOperationTest {

    private SnowflakeWriteOperation writeOperation;

    private SnowflakeSink sink = new SnowflakeSink();

    @Before
    public void setup() {
        writeOperation = new SnowflakeWriteOperation(sink);
        writeOperation.initialize(null);
    }

    @Test
    public void testGetSink() {
        Assert.assertEquals(sink, writeOperation.getSink());
    }

    @Test
    public void testFinalize() {
        List<Result> writerResults = Arrays.asList(
                new Result("result1", 2, 2, 0),
                new Result("result2", 3, 3, 0),
                new Result("result3", 1, 0, 1));

        Map<String, Object> finalizedResults = writeOperation.finalize(writerResults, null);

        Assert.assertFalse(finalizedResults.isEmpty());
        Assert.assertEquals(6, finalizedResults.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        Assert.assertEquals(5, finalizedResults.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        Assert.assertEquals(1, finalizedResults.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));

    }

    @Test
    public void testCreateWriter() {

        Writer<?> writer = writeOperation.createWriter(null);
        Assert.assertTrue(writer instanceof SnowflakeWriter);
    }
}
