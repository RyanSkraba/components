// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.splunk.runtime;

import java.util.Map;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.splunk.TSplunkEventCollectorDefinition;

public class SplunkWriterResult extends Result {

    int errorCode;

    String errorMessage;

    public SplunkWriterResult() {
        super();
    }

    public SplunkWriterResult(String uId, int totalRecords, int successCount, int rejectCount, int errorCode,
            String errorMessage) {
        super(uId, totalRecords);
        this.successCount = successCount;
        this.rejectCount = rejectCount;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> returnMap = super.toMap();
        returnMap.put(ComponentDefinition.RETURN_ERROR_MESSAGE, errorMessage);
        returnMap.put(TSplunkEventCollectorDefinition.RETURN_RESPONSE_CODE, errorCode);
        return returnMap;
    }

    @Override
    public void add(Result result) {
        super.add(result);
        SplunkWriterResult sresult = (SplunkWriterResult) result;
        // FIXME - for now just take the last one, but need to figure out how to properly accumulate these if there are
        // multiples.
        if (sresult.errorCode > 0)
            errorCode = sresult.errorCode;
        if (sresult.errorMessage != null)
            errorMessage = sresult.errorMessage;
    }

    @Override
    public String toString() {
        return super.toString() + " code: " + errorCode + " message: " + errorMessage;
    }
}
