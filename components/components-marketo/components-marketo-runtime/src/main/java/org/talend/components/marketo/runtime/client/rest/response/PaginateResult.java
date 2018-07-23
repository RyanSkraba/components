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
package org.talend.components.marketo.runtime.client.rest.response;

public abstract class PaginateResult extends RequestResult {

    protected String nextPageToken;

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(getClass().getSimpleName());
        sb.append("{requestId='").append(requestId).append('\'');
        sb.append(", success=").append(success);
        sb.append(", errors=").append(errors);
        sb.append(", result=").append(getResult());
        sb.append(", moreResult=").append(moreResult);
        sb.append(", nextPageToken=").append(nextPageToken);
        sb.append('}');
        return sb.toString();
    }
}
