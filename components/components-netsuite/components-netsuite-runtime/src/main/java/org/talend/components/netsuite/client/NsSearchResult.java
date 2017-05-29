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
 * Holds search result's data.
 *
 * <p>This data object is mirror of NetSuite's native {@code SearchResult} data object.
 *
 * @param <RecT> type of record data object
 */
public class NsSearchResult<RecT> {

    /** Status of 'search' operation. */
    private NsStatus status;

    private Integer totalRecords;

    private Integer pageSize;

    private Integer totalPages;

    private Integer pageIndex;

    private String searchId;

    private List<RecT> recordList;

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

    public boolean isSuccess() {
        return status.isSuccess();
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
        sb.append(", success=").append(isSuccess());
        sb.append(", totalRecords=").append(totalRecords);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", totalPages=").append(totalPages);
        sb.append(", pageIndex=").append(pageIndex);
        sb.append(", searchId='").append(searchId).append('\'');
        sb.append(", recordList=").append(recordList);
        sb.append('}');
        return sb.toString();
    }
}
