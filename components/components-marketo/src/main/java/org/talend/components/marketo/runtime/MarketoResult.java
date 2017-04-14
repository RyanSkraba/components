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
package org.talend.components.marketo.runtime;

import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;

public class MarketoResult extends Result {

    public int apiCalls;

    public MarketoResult() {
        super();
    }

    public MarketoResult(String uId, int totalCount, int successCount, int rejectCount, int apiCalls) {
        super(uId, totalCount, successCount, rejectCount);
        this.apiCalls = apiCalls;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> returnMap = super.toMap();
        returnMap.put(RETURN_NB_CALL, apiCalls);
        return returnMap;
    }

    @Override
    public void add(Result result) {
        super.add(result);
        apiCalls += ((MarketoResult) result).getApiCalls();
    }

    @Override
    public String toString() {
        return super.toString() + " API calls: " + apiCalls;
    }

    public int getApiCalls() {
        return apiCalls;
    }

    public void setApiCalls(int apiCalls) {
        this.apiCalls = apiCalls;
    }
}
