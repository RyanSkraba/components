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
package org.talend.components.marketo.runtime.client.type;

import java.util.ArrayList;
import java.util.List;

public abstract class MarketoBaseResult<T> {

    String requestId = "";

    boolean success;

    List<MarketoError> errors;

    String streamPosition = "";

    int recordCount;

    int remainCount;

    public MarketoBaseResult(String streamPosition, int recordCount, int remainCount, List<?> records) {
        this.streamPosition = streamPosition;
        this.recordCount = recordCount;
        this.remainCount = remainCount;
        this.setRecords((List<T>) records);
    }

    public MarketoBaseResult() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<MarketoError> getErrors() {
        // ensure that errors is never null
        if (errors == null) {
            return new ArrayList<>();
        }
        return errors;
    }

    public void setErrors(List<MarketoError> errors) {
        this.errors = errors;
    }

    public String getStreamPosition() {
        return streamPosition;
    }

    public void setStreamPosition(String streamPosition) {
        this.streamPosition = streamPosition;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getRemainCount() {
        return remainCount;
    }

    public void setRemainCount(int remainCount) {
        this.remainCount = remainCount;
    }

    public abstract List<T> getRecords();

    public abstract void setRecords(List<T> records);

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MarketoRecordResult{");
        sb.append("requestId='").append(requestId).append('\'');
        sb.append(", success=").append(success);
        sb.append(", errors=").append(errors);
        sb.append(", recordCount=").append(recordCount);
        sb.append(", remainCount=").append(remainCount);
        sb.append(", streamPosition='").append(streamPosition).append('\'');
        sb.append(", records=").append(getRecords());
        sb.append('}');
        return sb.toString();
    }
}
