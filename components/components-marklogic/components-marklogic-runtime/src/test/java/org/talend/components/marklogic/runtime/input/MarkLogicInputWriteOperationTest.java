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

package org.talend.components.marklogic.runtime.input;

import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class MarkLogicInputWriteOperationTest {

    @Test
    public void testGetSink() {
        MarkLogicInputSink inputSink = new MarkLogicInputSink();
        MarkLogicInputWriteOperation inputWriteOperation = inputSink.createWriteOperation();

        assertEquals(inputSink, inputWriteOperation.getSink());
    }

    @Test
    public void testInitialize() {
        RuntimeContainer mockedContainer = mock(RuntimeContainer.class);
        MarkLogicInputWriteOperation inputWriteOperation = new MarkLogicInputWriteOperation(null, null);

        inputWriteOperation.initialize(mockedContainer);

        verifyZeroInteractions(mockedContainer);
    }

    @Test
    public void testCreateWriter() {
        MarkLogicInputWriteOperation inputWriteOperation = new MarkLogicInputWriteOperation(null, null);
        MarkLogicRowProcessor reader = inputWriteOperation.createWriter(null);

        assertNotNull(reader);
    }

    @Test
    public void testFinalize() {
        MarkLogicInputWriteOperation inputWriteOperation = new MarkLogicInputWriteOperation(null, null);
        List<Result> resultList = new ArrayList<>();
        ResultWithLongNB r1 = new ResultWithLongNB();
        ResultWithLongNB r2 = new ResultWithLongNB();
        r1.successCountLong = 2;
        r2.successCountLong = 1;

        r1.rejectCountLong = 4;
        r2.rejectCountLong = 1;

        r1.totalCountLong = 6;
        r2.totalCountLong = 2;

        resultList.add(r1);
        resultList.add(r2);
        Map<String, Object> finalResultsMap = inputWriteOperation.finalize(resultList, null);

        assertEquals(3,finalResultsMap.size());
        assertEquals(3L, finalResultsMap.get("successRecordCount"));
        assertEquals(5L, finalResultsMap.get("rejectRecordCount"));
        assertEquals(8L, finalResultsMap.get("totalRecordCount"));
    }
}
