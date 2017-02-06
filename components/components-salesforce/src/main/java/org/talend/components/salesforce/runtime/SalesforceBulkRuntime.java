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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.SalesforceBulkProperties.Concurrency;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.daikon.exception.ExceptionContext;
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

    private SalesforceSource sfSource;

    public SalesforceBulkRuntime(SalesforceSource sfSource, RuntimeContainer container) throws IOException {
        this.sfSource = sfSource;
        this.bulkConnection = sfSource.connect(container).bulkConnection;
        if (this.bulkConnection == null) {
            throw new RuntimeException(
                    "Please check \"Bulk Connection\" checkbox in the setting of the referenced tSalesforceConnection.");
        }
    }

    public BulkConnection getBulkConnection() {
        return bulkConnection;
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
        // System.out.println(job);
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
            // System.out.println(batchInfo);
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
    private void closeJob() throws AsyncApiException, ConnectionException {
        JobInfo closeJob = new JobInfo();
        closeJob.setId(job.getId());
        closeJob.setState(JobStateEnum.Closed);
        updateJob(closeJob);
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
            // System.out.println("Awaiting results..." + incomplete.size());
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

    public void doBulkQuery(String moduleName, String queryStatement, int secToWait)
            throws AsyncApiException, InterruptedException, ConnectionException {
        job = new JobInfo();
        job.setObject(moduleName);
        job.setOperation(OperationEnum.query);
        if (concurrencyMode != null) {
            job.setConcurrencyMode(concurrencyMode);
        }
        job.setContentType(ContentType.CSV);
        job = createJob(job);

        job = getJobStatus(job.getId());
        batchInfoList = new ArrayList<BatchInfo>();
        BatchInfo info = null;
        ByteArrayInputStream bout = new ByteArrayInputStream(queryStatement.getBytes());
        info = createBatchFromStream(job, bout);

        while (true) {
            Thread.sleep(secToWait * 1000); // default is 30 sec
            info = getBatchInfo(job.getId(), info.getId());

            if (info.getState() == BatchStateEnum.Completed) {
                QueryResultList list = getQueryResultList(job.getId(), info.getId());
                queryResultIDs = new HashSet<String>(Arrays.asList(list.getResult())).iterator();
                break;
            } else if (info.getState() == BatchStateEnum.Failed) {
                throw new ComponentException(new DefaultErrorCode(HttpServletResponse.SC_BAD_REQUEST, "failedBatch"),
                        ExceptionContext.build().put("failedBatch", info));
            } else {
                System.out.println("-------------- waiting ----------" + info);
            }
        }
        batchInfoList.add(info);
        closeJob();
    }

    public BulkResultSet getQueryResultSet(String resultId) throws AsyncApiException, IOException, ConnectionException {
        baseFileReader = new com.csvreader.CsvReader(new BufferedReader(new InputStreamReader(
                getQueryResultStream(job.getId(), batchInfoList.get(0).getId(), resultId), FILE_ENCODING)), ',');

        if (baseFileReader.readRecord()) {
            baseFileHeader = Arrays.asList(baseFileReader.getValues());
        }
        return new BulkResultSet(baseFileReader, baseFileHeader);
    }

    protected JobInfo createJob(JobInfo job) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.createJob(job);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                sfSource.renewSession(bulkConnection.getConfig());
                return createJob(job);
            }
            throw sfException;
        }
    }

    protected BatchInfo createBatchFromStream(JobInfo job, InputStream input) throws AsyncApiException, ConnectionException {
        try {
            return bulkConnection.createBatchFromStream(job, input);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                sfSource.renewSession(bulkConnection.getConfig());
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
                sfSource.renewSession(bulkConnection.getConfig());
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
                sfSource.renewSession(bulkConnection.getConfig());
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
                sfSource.renewSession(bulkConnection.getConfig());
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
                sfSource.renewSession(bulkConnection.getConfig());
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
                sfSource.renewSession(bulkConnection.getConfig());
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
                sfSource.renewSession(bulkConnection.getConfig());
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
                sfSource.renewSession(bulkConnection.getConfig());
                return getQueryResultStream(jobID, batchID, resultID);
            }
            throw sfException;
        }
    }

    public String nextResultId() {
        String resultId = null;
        if (queryResultIDs != null && queryResultIDs.hasNext()) {
            resultId = queryResultIDs.next();
        }
        return resultId;
    }

    class BulkResultSet {

        com.csvreader.CsvReader reader;

        List<String> header;

        boolean hashNext = true;

        public BulkResultSet(com.csvreader.CsvReader reader, List<String> header) {
            this.reader = reader;
            this.header = header;
        }

        public BulkResult next() throws IOException {

            boolean hasNext = false;
            try {
                hasNext = reader.readRecord();
            } catch (IOException e) {
                if (this.reader != null) {
                    this.reader.close();
                }
                throw e;
            }

            BulkResult result = null;
            String[] row;

            if (hasNext) {
                if ((row = reader.getValues()) != null) {
                    result = new BulkResult();
                    for (int i = 0; i < this.header.size(); i++) {
                        result.setValue(header.get(i), row[i]);
                    }
                    return result;
                } else {
                    return next();
                }
            } else {
                if (this.reader != null) {
                    this.reader.close();
                }
            }
            return null;

        }

        public boolean hasNext() {
            return hashNext;
        }

    }

    class BulkResult {

        Map<String, Object> values;

        public BulkResult() {
            values = new HashMap<String, Object>();
        }

        public void setValue(String field, Object vlaue) {
            values.put(field, vlaue);
        }

        public Object getValue(String fieldName) {
            return values.get(fieldName);
        }

        public void copyValues(BulkResult result) {
            if (result == null) {
                return;
            } else {
                for (String key : result.values.keySet()) {
                    Object value = result.values.get(key);
                    if ("#N/A".equals(value)) {
                        value = null;
                    }
                    values.put(key, value);
                }
            }
        }
    }

}
