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
package org.talend.components.salesforce.runtime;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.SalesforceBulkProperties.Concurrency;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.common.SalesforceErrorCodes;
import org.talend.components.salesforce.runtime.common.SalesforceRuntimeCommon;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.DefaultErrorCode;

import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchInfoList;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.async.QueryResultList;
import com.sforce.ws.ConnectionException;

/**
 * This contains process a set of records by creating a job that contains one or more batches. The job specifies which
 * object is being processed and what type of action is being used (query, insert, upsert, update, or delete).
 */

public class SalesforceBulkRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceBulkRuntime.class.getName());

    private final String FILE_ENCODING = "UTF-8";

    private String sObjectType;

    private OperationEnum operation;

    private String externalIdFieldName;

    private ContentType contentType;

    private String bulkFileName;

    private int maxBytesPerBatch;

    private int maxRowsPerBatch;

    private List<BatchInfo> batchInfoList;

    private BufferedReader br;

    private JobInfo job;

    private com.csvreader.CsvReader baseFileReader;

    private List<String> baseFileHeader;

    private BulkConnection bulkConnection;

    private ConcurrencyMode concurrencyMode = null;

    private Iterator<String> queryResultIDs = null;

    private long awaitTime = 10000L;

    private int chunkSize;

    private static final String PK_CHUNKING_HEADER_NAME = "Sforce-Enable-PKChunking";

    private static final String CHUNK_SIZE_PROPERTY_NAME = "chunkSize=";

    public SalesforceBulkRuntime(BulkConnection bulkConnection) throws IOException {
        this.bulkConnection = bulkConnection;
        if (this.bulkConnection == null) {
            throw new RuntimeException(
                    "Please check \"Bulk Connection\" checkbox in the setting of the referenced tSalesforceConnection.");
        }
    }

    public BulkConnection getBulkConnection() {
        return bulkConnection;
    }

    /**
     * Set value for chunk size. Maximum value is 250_000.
     *
     * @param chunkSize - value for batches size.
     */
    public void setChunkSize(int chunkSize) {
        if (chunkSize > TSalesforceInputProperties.MAX_CHUNK_SIZE) {
            LOGGER.warn("Chunk size was set to max value - 250000.");
            this.chunkSize = TSalesforceInputProperties.MAX_CHUNK_SIZE;
        } else if (chunkSize <= 0){
            LOGGER.warn("Chunk size was set to default value - 100000.");
            this.chunkSize = TSalesforceInputProperties.DEFAULT_CHUNK_SIZE;
        } else {
            this.chunkSize = chunkSize;
        }
    }

    private void setBulkOperation(String sObjectType, OutputAction userOperation, String externalIdFieldName,
            String contentTypeStr, String bulkFileName, int maxBytes, int maxRows) {
        this.sObjectType = sObjectType;
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
            operation = OperationEnum.insert;
            break;
        }
        this.externalIdFieldName = externalIdFieldName;

        if ("csv".equals(contentTypeStr)) {
            contentType = ContentType.CSV;
        } else if ("xml".equals(contentTypeStr)) {
            contentType = ContentType.XML;
        }
        this.bulkFileName = bulkFileName;

        int sforceMaxBytes = 10 * 1024 * 1024;
        int sforceMaxRows = 10000;
        maxBytesPerBatch = (maxBytes > sforceMaxBytes) ? sforceMaxBytes : maxBytes;
        maxRowsPerBatch = (maxRows > sforceMaxRows) ? sforceMaxRows : maxRows;
    }

    public void executeBulk(String sObjectType, OutputAction userOperation, String externalIdFieldName, String contentTypeStr,
            String bulkFileName, int maxBytes, int maxRows) throws AsyncApiException, ConnectionException, IOException {
        setBulkOperation(sObjectType, userOperation, externalIdFieldName, contentTypeStr, bulkFileName, maxBytes, maxRows);
        job = createJob();
        batchInfoList = createBatchesFromCSVFile();
        closeJob();
        awaitCompletion();
        prepareLog();
    }

    private void prepareLog() throws IOException {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(bulkFileName), FILE_ENCODING));
        baseFileReader = new com.csvreader.CsvReader(br, ',');
        if (baseFileReader.readRecord()) {
            baseFileHeader = Arrays.asList(baseFileReader.getValues());
        }
    }

    public void setConcurrencyMode(Concurrency mode) {
        switch (mode) {
        case Parallel:
            concurrencyMode = ConcurrencyMode.Parallel;
            break;
        case Serial:
            concurrencyMode = ConcurrencyMode.Serial;
            break;

        default:
            break;
        }
    }

    /**
     * Create a new job using the Bulk API.
     *
     * @return The JobInfo for the new job.
     * @throws AsyncApiException
     * @throws ConnectionException
     */
    private JobInfo createJob() throws AsyncApiException, ConnectionException {
        JobInfo job = new JobInfo();
        if (concurrencyMode != null) {
            job.setConcurrencyMode(concurrencyMode);
        }
        job.setObject(sObjectType);
        job.setOperation(operation);
        if (OperationEnum.upsert.equals(operation)) {
            job.setExternalIdFieldName(externalIdFieldName);
        }
        job.setContentType(contentType);
        job = createJob(job);
        return job;
    }

    private int countQuotes(String value) {
        if (value == null || "".equals(value)) {
            return 0;
        } else {
            char c = '\"';
            int num = 0;
            char[] chars = value.toCharArray();
            for (char d : chars) {
                if (c == d) {
                    num++;
                }
            }
            return num;
        }
    }

    /**
     * Create and upload batches using a CSV file. The file into the appropriate size batch files.
     *
     * @return
     * @throws IOException
     * @throws AsyncApiException
     * @throws ConnectionException
     */
    private List<BatchInfo> createBatchesFromCSVFile() throws IOException, AsyncApiException, ConnectionException {
        List<BatchInfo> batchInfos = new ArrayList<BatchInfo>();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(bulkFileName), FILE_ENCODING));
        // read the CSV header row
        byte[] headerBytes = (rdr.readLine() + "\n").getBytes("UTF-8");
        int headerBytesLength = headerBytes.length;
        File tmpFile = File.createTempFile("sforceBulkAPI", ".csv");
        // Split the CSV file into multiple batches
        try {
            FileOutputStream tmpOut = new FileOutputStream(tmpFile);
            int currentBytes = 0;
            int currentLines = 0;
            String nextLine;
            boolean needStart = true;
            boolean needEnds = true;
            while ((nextLine = rdr.readLine()) != null) {
                int num = countQuotes(nextLine);
                // nextLine is header or footer of the record
                if (num % 2 == 1) {
                    if (!needStart) {
                        needEnds = false;
                    } else {
                        needStart = false;
                    }
                } else {
                    // nextLine is a whole record or middle of the record
                    if (needEnds && needStart) {
                        needEnds = false;
                        needStart = false;
                    }
                }

                byte[] bytes = (nextLine + "\n").getBytes("UTF-8");

                // Create a new batch when our batch size limit is reached
                if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
                    createBatch(tmpOut, tmpFile, batchInfos);
                    currentBytes = 0;
                    currentLines = 0;
                }
                if (currentBytes == 0) {
                    tmpOut = new FileOutputStream(tmpFile);
                    tmpOut.write(headerBytes);
                    currentBytes = headerBytesLength;
                    currentLines = 1;
                }
                tmpOut.write(bytes);
                currentBytes += bytes.length;
                if (!needStart && !needEnds) {
                    currentLines++;
                    needStart = true;
                    needEnds = true;
                }
            }
            // Finished processing all rows
            // Create a final batch for any remaining data
            rdr.close();
            if (currentLines > 1) {
                createBatch(tmpOut, tmpFile, batchInfos);
            }
        } finally {
            tmpFile.delete();
        }
        return batchInfos;
    }

    /**
     * Create a batch by uploading the contents of the file. This closes the output stream.
     *
     * @param tmpOut The output stream used to write the CSV data for a single batch.
     * @param tmpFile The file associated with the above stream.
     * @param batchInfos The batch info for the newly created batch is added to this list.
     *
     * @throws IOException
     * @throws AsyncApiException
     * @throws ConnectionException
     */
    private void createBatch(FileOutputStream tmpOut, File tmpFile, List<BatchInfo> batchInfos)
            throws IOException, AsyncApiException, ConnectionException {
        tmpOut.flush();
        tmpOut.close();
        FileInputStream tmpInputStream = new FileInputStream(tmpFile);
        try {
            BatchInfo batchInfo = createBatchFromStream(job, tmpInputStream);
            batchInfos.add(batchInfo);
        } finally {
            tmpInputStream.close();
        }
    }

    /**
     * Close the job
     *
     * @throws AsyncApiException
     * @throws ConnectionException
     */
    void closeJob() throws AsyncApiException, ConnectionException {
        JobInfo closeJob = new JobInfo();
        closeJob.setId(job.getId());
        closeJob.setState(JobStateEnum.Closed);
        try {
            bulkConnection.updateJob(closeJob);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                closeJob();
            } else if (AsyncExceptionCode.InvalidJobState.equals(sfException.getExceptionCode())) {
                // Job is already closed on Salesforce side. We don't need to close it again.
                return;
            }
            throw sfException;
        }
    }

    public void setAwaitTime(long awaitTime) {
        this.awaitTime = awaitTime;
    }

    /**
     * Wait for a job to complete by polling the Bulk API.
     *
     * @throws AsyncApiException
     * @throws ConnectionException
     */
    private void awaitCompletion() throws AsyncApiException, ConnectionException {
        long sleepTime = 0L;
        Set<String> incomplete = new HashSet<String>();
        for (BatchInfo bi : batchInfoList) {
            incomplete.add(bi.getId());
        }
        while (!incomplete.isEmpty()) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
            sleepTime = awaitTime;
            BatchInfo[] statusList = getBatchInfoList(job.getId()).getBatchInfo();
            for (BatchInfo b : statusList) {
                if (b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) {
                    incomplete.remove(b.getId());
                }
            }
        }
    }

    /**
     * Get result from the reader
     *
     * @return
     * @throws IOException
     */
    private BulkResult getBaseFileRow() throws IOException {
        BulkResult dataInfo = new BulkResult();
        if (baseFileReader.readRecord()) {
            List<String> row = Arrays.asList(baseFileReader.getValues());
            for (int i = 0; i < row.size(); i++) {
                dataInfo.setValue(baseFileHeader.get(i), row.get(i));
            }
        }
        return dataInfo;
    }

    /**
     * Gets the results of the operation and checks for errors.
     *
     * @param batchNum
     * @return
     * @throws AsyncApiException
     * @throws IOException
     * @throws ConnectionException
     */
    public List<BulkResult> getBatchLog(int batchNum) throws AsyncApiException, IOException, ConnectionException {
        // batchInfoList was populated when batches were created and submitted
        List<BulkResult> resultInfoList = new ArrayList<BulkResult>();
        BulkResult resultInfo;
        BatchInfo b = batchInfoList.get(batchNum);
        CSVReader rdr = new CSVReader(getBatchResultStream(job.getId(), b.getId()));

        List<String> resultHeader = rdr.nextRecord();
        int resultCols = resultHeader.size();
        List<String> row;
        while ((row = rdr.nextRecord()) != null) {
            resultInfo = new BulkResult();
            resultInfo.copyValues(getBaseFileRow());
            for (int i = 0; i < resultCols; i++) {
                String header = resultHeader.get(i);
                resultInfo.setValue(header, row.get(i));

                if ("Created".equals(header)) {
                    resultInfo.setValue("salesforce_created", row.get(i));
                } else if ("Id".equals(header)) {
                    resultInfo.setValue("salesforce_id", row.get(i));
                }
            }
            resultInfoList.add(resultInfo);
        }

        return resultInfoList;
    }

    public int getBatchCount() {
        return batchInfoList.size();
    }

    /**
     * Creates and executes job for bulk query. Job must be finished in 2 minutes on Salesforce side.<br/>
     * From Salesforce documentation two scenarios are possible here:
     * <ul>
     * <li>simple bulk query. It should have status - {@link BatchStateEnum#Completed}.</li>
     * <li>primary key chunking bulk query. It should return first batch info with status - {@link BatchStateEnum#NotProcessed}.<br/>
     * Other batch info's should have status - {@link BatchStateEnum#Completed}</li>
     * </ul>
     *
     * @param moduleName - input module name.
     * @param queryStatement - to be executed.
     * @throws AsyncApiException
     * @throws InterruptedException
     * @throws ConnectionException
     */
    public void doBulkQuery(String moduleName, String queryStatement)
            throws AsyncApiException, InterruptedException, ConnectionException {
        job = new JobInfo();
        job.setObject(moduleName);
        job.setOperation(OperationEnum.query);
        if (concurrencyMode != null) {
            job.setConcurrencyMode(concurrencyMode);
        }
        job.setContentType(ContentType.CSV);
        job = createJob(job);
        if (job.getId() == null) { // job creation failed
            throw new ComponentException(new DefaultErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "failedBatch"),
                    ExceptionContext.build().put("failedBatch", job));
        }

        ByteArrayInputStream bout = new ByteArrayInputStream(queryStatement.getBytes());
        BatchInfo info = createBatchFromStream(job, bout);
        int secToWait = 1;
        int tryCount = 0;
        while (true) {
            LOGGER.debug("Awaiting " + secToWait + " seconds for results ...\n" + info);
            Thread.sleep(secToWait * 1000);
            info = getBatchInfo(job.getId(), info.getId());

            if (info.getState() == BatchStateEnum.Completed
                    || (BatchStateEnum.NotProcessed == info.getState() && 0 < chunkSize)) {
                break;
            } else if (info.getState() == BatchStateEnum.Failed) {
                throw new ComponentException(new DefaultErrorCode(HttpServletResponse.SC_BAD_REQUEST, "failedBatch"),
                        ExceptionContext.build().put("failedBatch", info));
            }
            tryCount++;
            if (tryCount % 3 == 0) {// after 3 attempt to get the result we multiply the time to wait by 2
                secToWait = secToWait * 3;
            }
            // There is also a 2-minute limit on the time to process the query.
            // If the query takes more than 2 minutes to process, a QUERY_TIMEOUT error is returned.
            // https://developer.salesforce.com/docs/atlas.en-us.api_asynch.meta/api_asynch/asynch_api_concepts_limits.htm
            int processingTime = (int) ((System.currentTimeMillis() - job.getCreatedDate().getTimeInMillis()) / 1000);
            if (processingTime > 120) {
                throw new ComponentException(new DefaultErrorCode(HttpServletResponse.SC_REQUEST_TIMEOUT, "failedBatch"),
                        ExceptionContext.build().put("failedBatch", info));
            }
        }

        retrieveResultsOfQuery(info);
    }

    public BulkResultSet getQueryResultSet(String resultId) throws AsyncApiException, IOException, ConnectionException {
        baseFileReader = new com.csvreader.CsvReader(new BufferedReader(
                new InputStreamReader(getQueryResultStream(job.getId(), batchInfoList.get(0).getId(), resultId), FILE_ENCODING)),
                ',');

        if (baseFileReader.readRecord()) {
            baseFileHeader = Arrays.asList(baseFileReader.getValues());
        }
        return new BulkResultSet(baseFileReader, baseFileHeader);
    }

    protected JobInfo createJob(JobInfo job) throws AsyncApiException, ConnectionException {
        try {
            if (0 != chunkSize) {
                // Enabling PK chunking by setting header and chunk size.
                bulkConnection.addHeader(PK_CHUNKING_HEADER_NAME, CHUNK_SIZE_PROPERTY_NAME + chunkSize);
            }
            return bulkConnection.createJob(job);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return createJob(job);
            }
            throw sfException;
        } finally {
            if (0 != chunkSize) {
                // Need to disable PK chunking after job was created.
                bulkConnection.addHeader(PK_CHUNKING_HEADER_NAME, Boolean.FALSE.toString());
            }
        }
    }

    protected BatchInfo createBatchFromStream(JobInfo job, InputStream input) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.createBatchFromStream(job, input);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return createBatchFromStream(job, input);
            }
            throw sfException;
        }
    }

    protected JobInfo updateJob(JobInfo job) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.updateJob(job);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return updateJob(job);
            }
            throw sfException;
        }
    }

    protected BatchInfoList getBatchInfoList(String jobID) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.getBatchInfoList(jobID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return getBatchInfoList(jobID);
            }
            throw sfException;
        }
    }

    protected InputStream getBatchResultStream(String jobID, String batchID) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.getBatchResultStream(jobID, batchID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return getBatchResultStream(jobID, batchID);
            }
            throw sfException;
        }
    }

    protected JobInfo getJobStatus(String jobID) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.getJobStatus(jobID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return getJobStatus(jobID);
            }
            throw sfException;
        }
    }

    protected BatchInfo getBatchInfo(String jobID, String batchID) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.getBatchInfo(jobID, batchID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return getBatchInfo(jobID, batchID);
            }
            throw sfException;
        }
    }

    public void close() throws IOException {
        if (br != null) {
            br.close();
        }
    }

    protected QueryResultList getQueryResultList(String jobID, String batchID) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.getQueryResultList(jobID, batchID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return getQueryResultList(jobID, batchID);
            }
            throw sfException;
        }
    }

    protected InputStream getQueryResultStream(String jobID, String batchID, String resultID)
            throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.getQueryResultStream(jobID, batchID, resultID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                SalesforceRuntimeCommon.renewSession(bulkConnection.getConfig());
                return getQueryResultStream(jobID, batchID, resultID);
            }
            throw sfException;
        }
    }

    /**
     * Retrieve resultId(-s) from job batches info.
     * Results will be retrieved only from completed batches.
     *
     * @param info - batch info from created job.
     * @throws AsyncApiException
     * @throws ConnectionException
     */
    private void retrieveResultsOfQuery(BatchInfo info) throws AsyncApiException, ConnectionException {

        if (BatchStateEnum.Completed == info.getState()) {
            QueryResultList list = getQueryResultList(job.getId(), info.getId());
            queryResultIDs = new HashSet<String>(Arrays.asList(list.getResult())).iterator();
            this.batchInfoList = Collections.singletonList(info);
            return;
        }

        /*
         * When pk chunking is enabled, we need to go through all batches in the job.
         * More information on Salesforce documentation:
         * https://developer.salesforce.com/docs/atlas.en-us.api_asynch.meta/api_asynch/asynch_api_code_curl_walkthrough_pk_chunking.htm
         */
        BatchInfoList batchInfoList = getBatchInfoList(job.getId());
        // BatchInfo list has a default size 0;
        Set<String> resultSet = new HashSet<>(batchInfoList.getBatchInfo().length);
        for (BatchInfo batch : batchInfoList.getBatchInfo()) {
            // BatchStateEnum.InProgress what is the workflow for this status?(should we wait or skip)
            if (batch.getId().equals(info.getId())) {
                continue;
            } else if (BatchStateEnum.Completed == batch.getState()) {
                resultSet.addAll(Arrays.asList(getQueryResultList(job.getId(), batch.getId()).getResult()));
            } else if (BatchStateEnum.Failed == batch.getState()) {
                //What if only 1 batch failed and others succeeded.
                TalendRuntimeException.build(SalesforceErrorCodes.ERROR_IN_BULK_QUERY_PROCESSING)
                        .put(ExceptionContext.KEY_MESSAGE, batch.getStateMessage()).throwIt();
            }
        }

        queryResultIDs = resultSet.iterator();
        this.batchInfoList = Arrays.asList(batchInfoList.getBatchInfo());
    }

    public String nextResultId() {
        String resultId = null;
        if (queryResultIDs != null && queryResultIDs.hasNext()) {
            resultId = queryResultIDs.next();
        }
        return resultId;
    }

    public boolean hasNextResultId() {
        return queryResultIDs != null && queryResultIDs.hasNext();
    }

}
