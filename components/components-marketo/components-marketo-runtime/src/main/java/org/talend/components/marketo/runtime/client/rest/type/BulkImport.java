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
package org.talend.components.marketo.runtime.client.rest.type;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.marketo.MarketoConstants;

public class BulkImport {

    /**
     * Unique integer id of the import batch
     */
    Integer batchId;

    /**
     * Time spent on the batch
     */
    String importTime;

    /**
     * 
     */
    String importId; // Leads only

    /**
     * Status message of the batch
     */
    String message;

    /**
     * Number of rows processed so far
     * 
     */
    Integer numOfLeadsProcessed; // Leads only

    /**
     * Number of rows processed so far
     */
    Integer numOfObjectsProcessed; // CO only

    /**
     * Number of rows failed so far
     */
    Integer numOfRowsFailed;

    /**
     * Number of rows with a warning so far
     */
    Integer numOfRowsWithWarning;

    /**
     * Object API Name
     */
    String objectApiName;

    /**
     * Bulk operation type. Can be import or export
     */
    String operation; // CO only

    /**
     * Status of the batch
     */
    String status;

    /**
     * Failures log filename
     */
    String failuresLogFile = "";

    /**
     * Warnings log filename
     */
    String warningsLogFile = "";

    // formats
    private String fileFormatCustomObject = "bulk_customobjects_%s_%d_%s.csv";

    private String fileFormatLead = "bulk_leads_%d_%s.csv";

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public String getImportTime() {
        return importTime;
    }

    public void setImportTime(String importTime) {
        this.importTime = importTime;
    }

    public String getImportId() {
        return importId;
    }

    public void setImportId(String importId) {
        this.importId = importId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getNumOfLeadsProcessed() {
        return numOfLeadsProcessed;
    }

    public void setNumOfLeadsProcessed(Integer numOfLeadsProcessed) {
        this.numOfLeadsProcessed = numOfLeadsProcessed;
    }

    public Integer getNumOfObjectsProcessed() {
        return numOfObjectsProcessed;
    }

    public void setNumOfObjectsProcessed(Integer numOfObjectsProcessed) {
        this.numOfObjectsProcessed = numOfObjectsProcessed;
    }

    public Integer getNumOfRowsFailed() {
        return numOfRowsFailed;
    }

    public void setNumOfRowsFailed(Integer numOfRowsFailed) {
        this.numOfRowsFailed = numOfRowsFailed;
    }

    public Integer getNumOfRowsWithWarning() {
        return numOfRowsWithWarning;
    }

    public void setNumOfRowsWithWarning(Integer numOfRowsWithWarning) {
        this.numOfRowsWithWarning = numOfRowsWithWarning;
    }

    public String getObjectApiName() {
        return objectApiName;
    }

    public void setObjectApiName(String objectApiName) {
        this.objectApiName = objectApiName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNumOfRowsProcessed() {
        return numOfLeadsProcessed != null ? numOfLeadsProcessed : numOfObjectsProcessed;
    }

    public String getFailuresLogFile() {
        return failuresLogFile;
    }

    public void setFailuresLogFile(String failuresLogFile) {
        this.failuresLogFile = failuresLogFile;
    }

    public String getWarningsLogFile() {
        return warningsLogFile;
    }

    public void setWarningsLogFile(String warningsLogFile) {
        this.warningsLogFile = warningsLogFile;
    }

    public String getFailuresOrWarningsFilename(Boolean isWarning) {
        String level = "failures";
        if (isWarning) {
            level = "warnings";
        }
        if (!StringUtils.isEmpty(objectApiName)) {
            return String.format(fileFormatCustomObject, getObjectApiName(), getBatchId(), level);
        } else {
            return String.format(fileFormatLead, getBatchId(), level);
        }
    }

    public boolean isBulkLeadsImport() {
        return StringUtils.isEmpty(objectApiName);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BulkImport{");
        sb.append("batchId=").append(batchId);
        sb.append(", importTime='").append(importTime).append('\'');
        sb.append(", importId='").append(importId).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", numOfRowsProcessed=").append(getNumOfRowsProcessed());
        sb.append(", numOfLeadsProcessed=").append(numOfLeadsProcessed);
        sb.append(", numOfObjectsProcessed=").append(numOfObjectsProcessed);
        sb.append(", numOfRowsFailed=").append(numOfRowsFailed);
        sb.append(", numOfRowsWithWarning=").append(numOfRowsWithWarning);
        sb.append(", objectApiName='").append(objectApiName).append('\'');
        sb.append(", operation='").append(operation).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", failuresLogFile='").append(failuresLogFile).append('\'');
        sb.append(", warningsLogFile='").append(warningsLogFile).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public IndexedRecord toIndexedRecord() {
        IndexedRecord record;
        if (isBulkLeadsImport()) {
            record = new GenericData.Record(MarketoConstants.getBulkImportLeadSchema());
            record.put(0, getBatchId());
            record.put(1, getImportId());
            record.put(2, getMessage());
            record.put(3, getNumOfLeadsProcessed());
            record.put(4, getNumOfRowsFailed());
            record.put(5, getNumOfRowsWithWarning());
            record.put(6, getStatus());
            record.put(7, getFailuresLogFile());
            record.put(8, getWarningsLogFile());

        } else {
            record = new GenericData.Record(MarketoConstants.getBulkImportCustomObjectSchema());
            record.put(0, getBatchId());
            record.put(1, getImportTime());
            record.put(2, getMessage());
            record.put(3, getNumOfObjectsProcessed());
            record.put(4, getNumOfRowsFailed());
            record.put(5, getNumOfRowsWithWarning());
            record.put(6, getObjectApiName());
            record.put(7, getOperation());
            record.put(8, getStatus());
            record.put(9, getFailuresLogFile());
            record.put(10, getWarningsLogFile());
        }
        return record;
    }
}
