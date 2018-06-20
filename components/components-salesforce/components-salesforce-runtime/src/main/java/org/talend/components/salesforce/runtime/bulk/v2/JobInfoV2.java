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

package org.talend.components.salesforce.runtime.bulk.v2;

import java.util.Date;

import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;

/**
 * Bulk API v2 JobInfo
 */
public class JobInfoV2 {

    /**
     * Default constructor
     */
    public JobInfoV2() {
    }

    /**
     * Unique ID for this job.
     */
    private String id;

    /**
     * The processing operation for the job.
     */
    private OperationEnum operation;

    /**
     * The object type for the data being processed.
     */
    private String object;

    /**
     * The ID of the user who created the job.
     */
    private String createdById;

    /**
     * The date and time in the UTC time zone when the job was created.
     */
    private Date createdDate;

    /**
     * Date and time in the UTC time zone when the job finished.
     */
    private Date systemModstamp;

    /**
     * The current state of processing for the job.
     */
    private JobStateEnum state;

    /**
     * The concurrency mode for the job.
     */
    private ConcurrencyMode concurrencyMode;

    /**
     * The format of the data being processed. Only CSV is supported.
     */
    private ContentType contentType;

    /**
     * The API version that the job was created in.
     */
    private String apiVersion;

    /**
     * The job’s type.
     */
    private String jobType;

    /**
     * The name of the external ID field for an upsert.
     */
    private String externalIdFieldName;

    /**
     * The String to use for Upload Job Data requests for this job. Only valid if the job is in Open state.
     */
    private String contentUrl;

    /**
     * The line ending used for CSV job data.
     */
    private String lineEnding;

    /**
     * The column delimiter used for CSV job data.
     */
    private String columnDelimiter;

    /**
     * The number of times that Salesforce attempted to save the results of an operation. The repeated attempts are due
     * to a
     * problem, such as a lock contention.
     */
    private int retries;

    /**
     * The number of milliseconds taken to process the job.
     */
    private long totalProcessingTime;

    /**
     * The number of milliseconds taken to process triggers and other processes related to the job data. This doesn't
     * include the
     * time used for processing asynchronous and batch Apex operations. If there are no triggers, the value is 0.
     */
    private long apiActiveProcessingTime;

    /**
     * The number of milliseconds taken to actively process the job and includes apexProcessingTime, but doesn't include
     * the time
     * the job waited in the queue to be processed or the time required for serialization and deserialization.
     */
    private long apexProcessingTime;

    /**
     * The number of records that were not processed successfully in this job.
     */
    private int numberRecordsFailed;

    /**
     * The number of records already processed.
     */
    private int numberRecordsProcessed;


    private String errorMessage;

    public String getId() {
        return id;
    }

    public OperationEnum getOperation() {
        return operation;
    }

    public String getObject() {
        return object;
    }

    public String getCreatedById() {
        return createdById;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getSystemModstamp() {
        return systemModstamp;
    }

    public JobStateEnum getState() {
        return state;
    }

    public ConcurrencyMode getConcurrencyMode() {
        return concurrencyMode;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getJobType() {
        return jobType;
    }

    public String getExternalIdFieldName() {
        return externalIdFieldName;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public String getLineEnding() {
        return lineEnding;
    }

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public int getRetries() {
        return retries;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public long getApiActiveProcessingTime() {
        return apiActiveProcessingTime;
    }

    public long getApexProcessingTime() {
        return apexProcessingTime;
    }

    public int getNumberRecordsFailed() {
        return numberRecordsFailed;
    }

    public int getNumberRecordsProcessed() {
        return numberRecordsProcessed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOperation(OperationEnum operation) {
        this.operation = operation;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setSystemModstamp(Date systemModstamp) {
        this.systemModstamp = systemModstamp;
    }

    public void setState(JobStateEnum state) {
        this.state = state;
    }

    public void setConcurrencyMode(ConcurrencyMode concurrencyMode) {
        this.concurrencyMode = concurrencyMode;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public void setExternalIdFieldName(String externalIdFieldName) {
        this.externalIdFieldName = externalIdFieldName;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public void setLineEnding(String lineEnding) {
        this.lineEnding = lineEnding;
    }

    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setTotalProcessingTime(long totalProcessingTime) {
        this.totalProcessingTime = totalProcessingTime;
    }

    public void setApiActiveProcessingTime(long apiActiveProcessingTime) {
        this.apiActiveProcessingTime = apiActiveProcessingTime;
    }

    public void setApexProcessingTime(long apexProcessingTime) {
        this.apexProcessingTime = apexProcessingTime;
    }

    public void setNumberRecordsFailed(int numberRecordsFailed) {
        this.numberRecordsFailed = numberRecordsFailed;
    }

    public void setNumberRecordsProcessed(int numberRecordsProcessed) {
        this.numberRecordsProcessed = numberRecordsProcessed;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[JobInfo ");

        sb.append(" id=");
        sb.append("'").append(getId()).append("'\n");
        sb.append(" operation=");
        sb.append("'").append(getOperation()).append("'\n");
        sb.append(" object=");
        sb.append("'").append(getObject()).append("'\n");
        sb.append(" createdById=");
        sb.append("'").append(getCreatedById()).append("'\n");
        sb.append(" createdDate=");
        sb.append("'").append(getCreatedDate()).append("'\n");
        sb.append(" systemModstamp=");
        sb.append("'").append(getSystemModstamp()).append("'\n");
        sb.append(" state=");
        sb.append("'").append(getState()).append("'\n");
        sb.append(" externalIdFieldName=");
        sb.append("'").append(getExternalIdFieldName()).append("'\n");
        sb.append(" concurrencyMode=");
        sb.append("'").append(getConcurrencyMode()).append("'\n");
        sb.append(" contentType=");
        sb.append("'").append(getContentType()).append("'\n");
        sb.append(" lineEnding=");
        sb.append("'").append(getLineEnding()).append("'\n");
        sb.append(" columnDelimiter=");
        sb.append("'").append(getColumnDelimiter()).append("'\n");
        sb.append(" contentUrl=");
        sb.append("'").append(getContentUrl()).append("'\n");
        sb.append(" jobType=");
        sb.append("'").append(getJobType()).append("'\n");
        sb.append(" numberRecordsProcessed=");
        sb.append("'").append(getNumberRecordsProcessed()).append("'\n");
        sb.append(" numberRetries=");
        sb.append("'").append(getRetries()).append("'\n");
        sb.append(" apiVersion=");
        sb.append("'").append(getApiVersion()).append("'\n");
        sb.append(" numberRecordsFailed=");
        sb.append("'").append(getNumberRecordsFailed()).append("'\n");
        sb.append(" totalProcessingTime=");
        sb.append("'").append(getTotalProcessingTime()).append("'\n");
        sb.append(" apiActiveProcessingTime=");
        sb.append("'").append(getApiActiveProcessingTime()).append("'\n");
        sb.append(" apexProcessingTime=");
        sb.append("'").append(getApexProcessingTime()).append("'\n");
        sb.append(" errorMessage=");
        sb.append("'").append(getErrorMessage()).append("'\n");
        sb.append("]\n");
        return sb.toString();
    }

}