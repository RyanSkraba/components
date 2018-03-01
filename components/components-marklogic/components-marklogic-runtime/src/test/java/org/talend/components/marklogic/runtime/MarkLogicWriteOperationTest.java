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

package org.talend.components.marklogic.runtime;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MarkLogicWriteOperationTest {
    MarkLogicWriteOperation writeOperation;
    MarkLogicSink sink;
    @Before
    public void setUp() {
        sink = new MarkLogicSink();
        MarkLogicOutputProperties outputProperties = new MarkLogicOutputProperties("outputProperties");
        writeOperation = new MarkLogicWriteOperation(sink, outputProperties);
    }

    @Test
    public void testInitialize() {
        //do nothing, for coverage
        writeOperation.initialize(null);
    }

    @Test
    public void testFinalize() {
        List<Result> resultList = new ArrayList<>();
        Result r1 = new Result();
        Result r2 = new Result();
        r1.successCount = 2;
        r2.successCount = 1;

        r1.rejectCount = 4;
        r2.rejectCount = 1;

        r1.totalCount = 6;
        r2.totalCount = 2;

        resultList.add(r1);
        resultList.add(r2);
        Map<String, Object> finalResults = writeOperation.finalize(resultList, null);

        assertTrue(finalResults.size() == 3);
        assertEquals(3, finalResults.get("successRecordCount"));
        assertEquals(5, finalResults.get("rejectRecordCount"));
        assertEquals(8, finalResults.get("totalRecordCount"));
    }

    @Test
    public void testGetWriter() {
        MarkLogicWriter writer = writeOperation.createWriter(null);

        assertNotNull(writer);
    }

    @Test
    public void testGetSink() {
        assertEquals(sink, writeOperation.getSink());
    }
}
