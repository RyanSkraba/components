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

package org.talend.components.adapter.beam.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;

public class AssertResultResult extends Result {

    public static final String ACTUAL_RESULT = "ACTUAL_RESULT";

    public List<String> actualResult;

    public AssertResultResult(){
        super();
        actualResult = new ArrayList<>();
    }

    public AssertResultResult(String uId, int totalCount, int successCount, int rejectCount, List<String> actualResult) {
        super(uId, totalCount, successCount, rejectCount);
        this.actualResult = actualResult;
    }

    @Override
    public void add(Result result) {
        super.add(result);
        actualResult.addAll(((AssertResultResult) result).actualResult);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> returnMap = super.toMap();
        returnMap.put(ACTUAL_RESULT, actualResult);
        return returnMap;
    }

    @Override
    public String toString() {
        return super.toString() + " actual rows: " + actualResult;
    }
}
