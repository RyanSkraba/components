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
package org.talend.components.api.component.runtime;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;

public class ResultTest {

    @Test
    public void testCreate() {
        Result wr = new Result();
        wr.totalCount = 12;
        wr.successCount = 8;
        wr.rejectCount = 4;
        assertEquals(12, wr.getTotalCount());
        assertEquals(8, wr.getSuccessCount());
        assertEquals(4, wr.getRejectCount());
    }

    @Test
    public void testMap() {
        Result wr = new Result();
        wr.totalCount = 12;
        wr.successCount = 8;
        wr.rejectCount = 4;
        Map<String, Object> resultMap = wr.toMap();
        assertEquals(12, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        assertEquals(8, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        assertEquals(4, resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));
    }

    @Test
    public void testAccumMap() {
        Result wr;
        List<Result> resultList = new ArrayList();

        wr = new Result();
        wr.totalCount = 12;
        wr.successCount = 8;
        wr.rejectCount = 4;
        resultList.add(wr);

        wr = new Result();
        wr.totalCount = 100;
        wr.successCount = 100;
        wr.rejectCount = 100;
        resultList.add(wr);

        Map<String, Object> resultMap = Result.accumulateAndReturnMap(resultList);

        assertEquals(112, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        assertEquals(108, resultMap.get(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT));
        assertEquals(104, resultMap.get(ComponentDefinition.RETURN_REJECT_RECORD_COUNT));
    }
}
