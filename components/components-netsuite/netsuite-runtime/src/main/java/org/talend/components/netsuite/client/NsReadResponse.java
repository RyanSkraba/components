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

package org.talend.components.netsuite.client;

public class NsReadResponse<RecT> {
    private NsStatus status;
    private RecT record;

    public NsReadResponse() {
    }

    public NsReadResponse(NsStatus status, RecT record) {
        this.status = status;
        this.record = record;
    }

    public NsStatus getStatus() {
        return status;
    }

    public RecT getRecord() {
        return record;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NsReadResponse{");
        sb.append("status=").append(status);
        sb.append(", record=").append(record);
        sb.append('}');
        return sb.toString();
    }
}
