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
package org.talend.components.salesforce.runtime.bulk.v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.runtime.BulkResultSet;
import org.talend.components.salesforce.runtime.bulk.v2.error.BulkV2ClientException;
import org.talend.components.salesforce.runtime.bulk.v2.request.CreateQueryJobRequest;
import org.talend.components.salesforce.runtime.bulk.v2.request.GetQueryJobResultRequest;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.csvreader.CsvReader;
import com.sforce.async.ContentType;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;

public class SalesforceBulkQueryV2Runtime {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceBulkQueryV2Runtime.class);

    private static final I18nMessages MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SalesforceBulkQueryV2Runtime.class);

    private String query;

    private ContentType contentType;

    private JobInfoV2 job;

    private BulkV2Connection bulkV2Connection;

    private boolean includeDeleted;

    private long jobTimeOut;

    private boolean safetySwitch = true;

    private boolean useResultLocator;

    private Integer maxRecords;

    private boolean hasMoreResult = true;

    private GetQueryJobResultRequest resultRequest;

    public SalesforceBulkQueryV2Runtime(BulkV2Connection bulkV2Connection, TSalesforceInputProperties sprops) {
        this.bulkV2Connection = bulkV2Connection;
        if (this.bulkV2Connection == null) {
            throw new RuntimeException(MESSAGES.getMessage("error.bulk.conn"));
        }
        if (sprops == null) {
            throw new RuntimeException(MESSAGES.getMessage("error.prop.config"));
        }
        // TODO need to check together with bulk v1 query
        setIncludeDeleted(sprops.includeDeleted.getValue());
        this.useResultLocator = sprops.useResultLocator.getValue();
        this.maxRecords =  sprops.maxRecords.getValue();
        // The content type for the job. The only valid value (and the default) is CSV
        if (true) {
            contentType = ContentType.CSV;
        }
    }

    /**
     * Set the global timeout of the job.
     *
     * @param properties - Salesforce input properties.
     */
    public void setJobTimeout(TSalesforceInputProperties properties) {
        Integer timeout = properties.jobTimeOut.getValue();
        if (timeout == null) {
            timeout = TSalesforceInputProperties.DEFAULT_JOB_TIME_OUT;
        }
        this.jobTimeOut = timeout * 1000; // from seconds to milliseconds
    }

    public void executeBulk() throws IOException, InterruptedException {
        // 1. create job
        job = createJob();
        LOGGER.info(MESSAGES.getMessage("info.job.create", job.toString()));

        // 2. check whether the job complete progress
        int secToWait = 1;
        int tryCount = 0;
        while (job.getState() == JobStateEnum.UploadComplete || job.getState() == JobStateEnum.InProgress) {
            LOGGER.debug("Awaiting " + secToWait + " seconds for results ...\n" + job.getId());
            Thread.sleep(secToWait * 1000);
            job = bulkV2Connection.getJobStatus(job.getId());
            tryCount++;
            if (tryCount % 3 == 0 && secToWait < 120) {
                secToWait = secToWait * 2;
            }
            if (jobTimeOut > 0) { // if 0, timeout is disabled
                long processingTime = System.currentTimeMillis() - job.getSystemModstamp().getTime();
                if (processingTime > jobTimeOut) {
                    throw new ComponentException(
                            new DefaultErrorCode(HttpServletResponse.SC_REQUEST_TIMEOUT, "failedBatch"),
                            ExceptionContext.build().put("failedBatch", job));
                }
            }
        }
        // 5. check the success and failed records.
        if (job.getState() == JobStateEnum.JobComplete) {
            LOGGER.info(MESSAGES.getMessage("info.result.success", job.getNumberRecordsProcessed()));
        } else {
            throw new BulkV2ClientException(job.getErrorMessage());
        }
    }

    public JobInfoV2 createJob() throws IOException {
        CreateQueryJobRequest request = new CreateQueryJobRequest();
        request.setQuery(query);
        if (includeDeleted) {
            request.setOperation(OperationEnum.queryAll);
        } else {
            request.setOperation(OperationEnum.query);
        }
        request.setContentType(contentType);
        return bulkV2Connection.createJob(request);
    }

    public BulkResultSet getResultSet(InputStream input) throws IOException {
        CsvReader reader = new CsvReader(new InputStreamReader(input), ',');
        reader.setSafetySwitch(safetySwitch);
        List<String> baseFileHeader = null;
        if (reader.readRecord()) {
            baseFileHeader = Arrays.asList(reader.getValues());
        }
        return new BulkResultSet(reader, baseFileHeader);
    }


    public BulkResultSet getResultSet() throws IOException {
        if (hasMoreResult) {
            if (this.useResultLocator) {
                if (resultRequest == null) {
                    resultRequest = new GetQueryJobResultRequest();
                    resultRequest.setQueryJobId(job.getId());
                    resultRequest.setMaxRecords(maxRecords);
                }
                InputStream inputStream = bulkV2Connection.getResult(resultRequest);
                if (resultRequest.getLocator() == null) {
                    hasMoreResult = false;
                }
                return getResultSet(inputStream);
            }else{
                hasMoreResult = false;
                return getResultSet(bulkV2Connection.getResult(job.getId()));
            }
        }else{
            return null;
        }
    }

    public void setSafetySwitch(boolean safetySwitch) {
        this.safetySwitch = safetySwitch;
    }

    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
