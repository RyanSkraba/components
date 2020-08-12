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

import com.sforce.async.ContentType;
import com.sforce.async.OperationEnum;

/**
 * Store request information for creating job
 */
public class CreateQueryJobRequest implements CreateJobRequest {

    /**
     * The content type for the job. The only valid value (and the default) is CSV.
     */
    private ContentType contentType;

    /**
     * The query to be performed.
     */
    private String query;

    /**
     * The processing operation for the job.
     *
     */
    private OperationEnum operation;

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String externalIdFieldName) {
        this.query = externalIdFieldName;
    }

    public OperationEnum getOperation() {
        return operation;
    }

    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

}
