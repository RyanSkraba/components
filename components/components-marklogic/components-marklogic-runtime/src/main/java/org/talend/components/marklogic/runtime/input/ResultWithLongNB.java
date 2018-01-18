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

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to support NB_LINE 'AFTER' return as Long type in runtime
 */
public class ResultWithLongNB extends Result {

    public long totalCountLong;

    public long successCountLong;

    public long rejectCountLong;

    public ResultWithLongNB() {
        super();
    }

    public ResultWithLongNB(String uId, long totalCounter) {
        super(uId);
        this.totalCountLong = totalCounter;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT, this.totalCountLong);
        map.put(ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT, this.successCountLong);
        map.put(ComponentDefinition.RETURN_REJECT_RECORD_COUNT, this.rejectCountLong);
        return map;
    }

    @Override
    public void add(Result result) {
        super.add(result);
        if (result instanceof ResultWithLongNB) {
            this.totalCountLong += ((ResultWithLongNB) result).totalCountLong;
            this.successCountLong += ((ResultWithLongNB) result).successCountLong;
            this.rejectCountLong += ((ResultWithLongNB) result).rejectCountLong;
        }
    }
}
