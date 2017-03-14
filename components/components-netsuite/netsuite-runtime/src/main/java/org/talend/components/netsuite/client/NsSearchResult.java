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

import java.util.List;

/**
 *
 */
public class NsSearchResult<RecT> {
    protected NsStatus status;
    protected Integer totalRecords;
    protected Integer pageSize;
    protected Integer totalPages;
    protected Integer pageIndex;
    protected String searchId;
    protected List<RecT> recordList;

    public boolean isSuccess() {
        return status.isSuccess();
    }

    public NsSearchResult() {
    }

    public NsSearchResult(NsStatus status) {
        this.status = status;
    }

    public NsStatus getStatus() {
        return status;
    }

    public void setStatus(NsStatus status) {
        this.status = status;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer value) {
        this.totalRecords = value;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer value) {
        this.pageSize = value;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer value) {
        this.totalPages = value;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer value) {
        this.pageIndex = value;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String value) {
        this.searchId = value;
    }

    public List<RecT> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<RecT> value) {
        this.recordList = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NsSearchResult{");
        sb.append("status=").append(status);
        sb.append(", totalRecords=").append(totalRecords);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", totalPages=").append(totalPages);
        sb.append(", pageIndex=").append(pageIndex);
        sb.append(", searchId='").append(searchId).append('\'');
        sb.append(", recordList=").append(recordList);
        sb.append(", success=").append(isSuccess());
        sb.append('}');
        return sb.toString();
    }
}
