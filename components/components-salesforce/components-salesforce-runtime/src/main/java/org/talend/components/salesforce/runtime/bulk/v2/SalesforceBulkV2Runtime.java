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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.salesforce.SalesforceBulkProperties;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.runtime.BulkResultSet;
import org.talend.components.salesforce.runtime.bulk.v2.error.BulkV2ClientException;
import org.talend.components.salesforce.runtime.bulk.v2.request.CreateJobRequest;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.csvreader.CsvReader;
import com.sforce.async.ContentType;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;

public class SalesforceBulkV2Runtime {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceBulkV2Runtime.class);

    private static final I18nMessages MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SalesforceBulkV2Runtime.class);

    private String objectType;

    private OperationEnum operation;

    private String externalIdFieldName;

    private ContentType contentType;

    private JobInfoV2 job;

    private long awaitTime = 10000L;

    private BulkV2Connection bulkConnection;

    private SalesforceBulkProperties.ColumnDelimiter columnDelimiter;

    private SalesforceBulkProperties.LineEnding lineEnding;

    private String bulkFileName;

    public SalesforceBulkV2Runtime(BulkV2Connection bulkConnection, TSalesforceBulkExecProperties sprops)
            throws BulkV2ClientException {
        this.bulkConnection = bulkConnection;
        if (this.bulkConnection == null) {
            throw new RuntimeException(MESSAGES.getMessage("error.bulk.conn"));
        }
        if (sprops == null) {
            throw new RuntimeException(MESSAGES.getMessage("error.prop.config"));
        }
        setColumnDelimiter(sprops.bulkProperties.columnDelimiter.getValue());
        setLineEnding(sprops.bulkProperties.lineEnding.getValue());
        setExternalIdFieldName(sprops.upsertKeyColumn.getStringValue());
        setObjectType(sprops.module.moduleName.getStringValue());
        setBulkFileName(sprops.bulkFilePath.getValue());
        setAwaitTime(sprops.bulkProperties.waitTimeCheckBatchState.getValue());
        intBulkOperation(sprops.outputAction.getValue());
    }

    private void intBulkOperation(OutputAction userOperation) throws BulkV2ClientException {
        switch (userOperation) {
        case INSERT:
            operation = OperationEnum.insert;
            break;
        case UPDATE:
            operation = OperationEnum.update;
            break;
        case UPSERT:
            operation = OperationEnum.upsert;
            break;
        case DELETE:
            operation = OperationEnum.delete;
            break;

        default:
            throw new BulkV2ClientException(MESSAGES.getMessage("error.bulkv2.unsupport", userOperation));
        }
        // The content type for the job. The only valid value (and the default) is CSV
        if (true) {
            contentType = ContentType.CSV;
        }
    }

    public void executeBulk() throws IOException, InterruptedException {
        // 1. create job
        job = createJob();
        LOGGER.info(MESSAGES.getMessage("info.job.create", job.toString()));
        // 2. upload data to job
        try (FileInputStream input = new FileInputStream(new File(bulkFileName))) {
            LOGGER.info(MESSAGES.getMessage("info.job.upload", job.getId()));
            bulkConnection.uploadDataFromStream(job.getId(), input);
        }
        // 3. close the job which would change the job static to "UploadComplete"
        job = bulkConnection.closeJob(job.getId());

        // 4. check whether the job complete progress
        while (job.getState() == JobStateEnum.UploadComplete || job.getState() == JobStateEnum.InProgress) {
            Thread.sleep(awaitTime);
            LOGGER.info(MESSAGES.getMessage("info.job.process", job.getId()));
            job = bulkConnection.getJobStatus(job.getId());
        }
        // 5. check the success and failed records.
        if (job.getState() == JobStateEnum.JobComplete) {
            LOGGER.info(MESSAGES.getMessage("info.result.success", job.getNumberRecordsProcessed()));
            LOGGER.info(MESSAGES.getMessage("info.result.failed", job.getNumberRecordsFailed()));
        } else {
            throw new BulkV2ClientException(job.getErrorMessage());
        }
    }

    public JobInfoV2 createJob() throws IOException {
        CreateJobRequest request = new CreateJobRequest();
        request.setObject(objectType);
        request.setOperation(operation);
        if (OperationEnum.upsert.equals(operation)) {
            request.setExternalIdFieldName(externalIdFieldName);
        }
        request.setContentType(contentType);
        request.setColumnDelimiter(columnDelimiter.toString());
        request.setLineEnding(lineEnding.toString());
        return bulkConnection.createJob(request);
    }

    protected JobInfoV2 getJobStatus(String jobID) throws IOException {
        return bulkConnection.getJobStatus(jobID);
    }

    public BulkResultSet getResultSet(InputStream input) throws IOException {
        CsvReader reader = new CsvReader(new InputStreamReader(input), getDelimitedChar(columnDelimiter));
        List<String> baseFileHeader = null;
        if (reader.readRecord()) {
            baseFileHeader = Arrays.asList(reader.getValues());
            Collections.replaceAll(baseFileHeader, "sf__Id", "salesforce_id");
            Collections.replaceAll(baseFileHeader, "sf__Created", "salesforce_created");
        }
        return new BulkResultSet(reader, baseFileHeader);
    }

    public void setColumnDelimiter(SalesforceBulkProperties.ColumnDelimiter columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public BulkResultSet getSuccessResultSet() throws IOException {
        return getResultSet(bulkConnection.getSuccessRecordsStream(job.getId()));
    }

    public BulkResultSet getFailedResultSet() throws IOException {
        return getResultSet(bulkConnection.getFailedRecordsStream(job.getId()));
    }

    public void setAwaitTime(long awaitTime) {
        this.awaitTime = awaitTime;
    }

    public SalesforceBulkProperties.LineEnding getLineEnding() {
        return lineEnding;
    }

    public void setLineEnding(SalesforceBulkProperties.LineEnding lineEnding) {
        this.lineEnding = lineEnding;
    }

    public String getExternalIdFieldName() {
        return externalIdFieldName;
    }

    public void setExternalIdFieldName(String externalIdFieldName) {
        this.externalIdFieldName = externalIdFieldName;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public void setBulkFileName(String bulkFileName) {
        this.bulkFileName = bulkFileName;
    }

    public int getNumberRecordsFailed() {
        if (job != null) {
            return job.getNumberRecordsFailed();
        }
        return 0;
    }

    public int getNumberRecordsProcessed() {
        if (job != null) {
            return job.getNumberRecordsProcessed();
        }
        return 0;
    }

    public char getDelimitedChar(SalesforceBulkProperties.ColumnDelimiter columnDelimiter) {
        if (columnDelimiter == null) {
            return ',';
        }
        switch (columnDelimiter) {
        case BACKQUOTE:
            return '`';
        case CARET:
            return '^';
        case COMMA:
            return ',';
        case PIPE:
            return '|';
        case SEMICOLON:
            return ';';
        case TAB:
            return '\t';
        default:
            return ',';
        }

    }
}
