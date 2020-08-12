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
public class CreateLoadJobRequest implements CreateJobRequest{

    /**
     * The column delimiter used for CSV job data. The default value is COMMA. Valid values refer to
     */
    private String columnDelimiter;

    /**
     * The content type for the job. The only valid value (and the default) is CSV.
     */
    private ContentType contentType;

    /**
     * The external ID field in the object being updated. Only needed for upsert operations. Field values must also
     * exist in CSV job data.
     */
    private String externalIdFieldName;

    /**
     * The line ending used for CSV job data, marking the end of a data row. The default is LF. Valid values refer to
     */
    private String lineEnding;

    /**
     * The object type for the data being processed. Use only a single object type per job.
     */
    private String object;

    /**
     * The processing operation for the job.
     *
     */
    private OperationEnum operation;

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getExternalIdFieldName() {
        return externalIdFieldName;
    }

    public void setExternalIdFieldName(String externalIdFieldName) {
        this.externalIdFieldName = externalIdFieldName;
    }

    public String getLineEnding() {
        return lineEnding;
    }

    public void setLineEnding(String lineEnding) {
        this.lineEnding = lineEnding;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public OperationEnum getOperation() {
        return operation;
    }

    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

}
