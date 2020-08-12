// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.salesforce.runtime.bulk.v2.request;

/**
 * Store request information for query results
 */
public class GetQueryJobResultRequest implements CreateJobRequest {

    /**
     * The ID of the query job.
     */
    private String queryJobId;

    /**
     * Gets a specific set of results.
     * If this parameter is omitted, the request returns as many rows as can be listed, starting at the beginning of the
     * results. If there are more results than can be listed, the response’s Sforce-Locator header contains the locator
     * value to get the next set of results.
     */
    private String locator;

    /**
     * The maximum number of records to retrieve per set. The request is still subject to the size limits.
     */

    private Integer maxRecords;

    public String getQueryJobId() {
        return queryJobId;
    }

    public void setQueryJobId(String queryJobId) {
        this.queryJobId = queryJobId;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public Integer getMaxRecords() {
        return maxRecords;
    }

    public void setMaxRecords(Integer maxRecords) {
        this.maxRecords = maxRecords;
    }

}
