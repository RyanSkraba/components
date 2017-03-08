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
package org.talend.components.azurestorage.table.runtime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.table.AzureStorageTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties.ActionOnTable;
import org.talend.components.common.runtime.GenericIndexedRecordConverter;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableServiceException;

public class AzureStorageTableWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    protected transient RuntimeContainer runtime;

    protected transient WriteOperation<Result> writeOperation;

    private transient Schema writeSchema;

    private IndexedRecord inputRecord;

    private IndexedRecordConverter<IndexedRecord, IndexedRecord> factory;

    private Result result;

    private AzureStorageTableSink sink;

    private TAzureStorageOutputTableProperties properties;

    private CloudTableClient client;

    private CloudTable table;

    private String tableName;

    private TAzureStorageOutputTableProperties.ActionOnData actionData;

    private TAzureStorageOutputTableProperties.ActionOnTable actionTable;

    private Boolean processOperationInBatch;

    private int batchOperationsCount;

    private List<TableOperation> batchOperations = new ArrayList<>();

    private List<IndexedRecord> batchRecords = new ArrayList<>();

    private String latestPartitionKey;

    private List<IndexedRecord> successfulWrites = new ArrayList<>();

    private List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private Map<String, String> nameMappings;

    private Boolean useNameMappings = Boolean.FALSE;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageTableWriter.class);

    public AzureStorageTableWriter(WriteOperation<Result> writeOperation, RuntimeContainer adaptor) {
        runtime = adaptor;
        this.writeOperation = writeOperation;
        sink = (AzureStorageTableSink) this.writeOperation.getSink();
        properties = sink.getProperties();
        tableName = properties.tableName.getValue();
        actionData = properties.actionOnData.getValue();
        actionTable = properties.actionOnTable.getValue();
        processOperationInBatch = properties.processOperationInBatch.getValue();
        nameMappings = properties.nameMapping.getNameMappings();
        if (nameMappings != null)
            useNameMappings = true;
    }

    @Override
    public void open(String uId) throws IOException {
        this.result = new Result(uId);
        if (writeSchema == null) {
            writeSchema = properties.schema.schema.getValue();
            if (AvroUtils.isIncludeAllFields(writeSchema)) {
                // if design schema include dynamic,need to get schema from record
                writeSchema = null;
            }
        }

        try {
            client = sink.getStorageTableClient(runtime);
            table = client.getTableReference(tableName);
            handleActionOnTable(properties.actionOnTable.getValue());
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new ComponentException(e);
        }
    }

    private void handleActionOnTable(ActionOnTable actionTable) throws IOException, StorageException {
        // FIXME How does this will behave in a distributed runtime ? See where to place correctly this
        // instruction...
        switch (actionTable) {
        case Create_table:
            table.create();
            break;
        case Create_table_if_does_not_exist:
            table.createIfNotExists();
            break;
        case Drop_and_create_table:
            table.delete();
            createTableAfterDeletion();
            break;
        case Drop_table_if_exist_and_create:
            table.deleteIfExists();
            createTableAfterDeletion();
            break;
        case Default:
        default:
            return;
        }

    }

    /**
     * This method create a table after it's deletion.<br/>
     * the table deletion take about 40 seconds to be effective on azure CF.
     * https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/Delete-Table#Remarks <br/>
     * So we try to wait 50 seconds if the first table creation return an
     * {@link StorageErrorCodeStrings.TABLE_BEING_DELETED } exception code
     * 
     * @throws StorageException
     * @throws IOException
     * 
     */
    private void createTableAfterDeletion() throws StorageException, IOException {
        try {
            table.create();
        } catch (TableServiceException e) {
            if (!e.getErrorCode().equals(StorageErrorCodeStrings.TABLE_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn("Table '{}' is currently being deleted. We'll retry in a few moments...", tableName);
            // wait 50 seconds (min is 40s) before retrying.
            // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/Delete-Table#Remarks
            try {
                Thread.sleep(50000);
            } catch (InterruptedException eint) {
                throw new IOException("Wait process for recreating table interrupted.");
            }
            table.create();
            LOGGER.debug("Table {} created.", tableName);
        }
    }

    @Override
    public void write(Object object) throws IOException {
        if (object == null) {
            return;
        }
        result.totalCount++;
        // This for dynamic which would get schema from the first record
        if (writeSchema == null) {
            writeSchema = ((IndexedRecord) object).getSchema();
        }
        inputRecord = getFactory(object).convertToAvro((IndexedRecord) object);
        DynamicTableEntity entity = new DynamicTableEntity();
        HashMap<String, EntityProperty> entityProps = new HashMap<>();
        for (Field f : writeSchema.getFields()) {
            String sName = f.name(); // schema name
            String mName = sName; // mapped name
            if (useNameMappings) {
                if (nameMappings.containsKey(sName)) {
                    mName = nameMappings.get(sName);
                }
            }
            Schema fSchema = f.schema();
            if (fSchema.getType() == Type.UNION)
                for (Schema s : f.schema().getTypes())
                    if (s.getType() != Type.NULL) {
                        fSchema = s;
                        break;
                    }
            if (sName.equals(AzureStorageTableProperties.TABLE_PARTITION_KEY)
                    || mName.equals(AzureStorageTableProperties.TABLE_PARTITION_KEY)) {
                entity.setPartitionKey((String) inputRecord.get(f.pos()));
            } else if (sName.equals(AzureStorageTableProperties.TABLE_ROW_KEY)
                    || mName.equals(AzureStorageTableProperties.TABLE_ROW_KEY)) {
                entity.setRowKey((String) inputRecord.get(f.pos()));
            } else if (sName.equals(AzureStorageTableProperties.TABLE_TIMESTAMP)
                    || mName.equals(AzureStorageTableProperties.TABLE_TIMESTAMP)) {
                // nop : managed by server
            } else { // that's some properties !
                if (fSchema.getType().equals(Type.BOOLEAN)) {
                    entityProps.put(mName, new EntityProperty((Boolean) inputRecord.get(f.pos())));
                } else if (fSchema.getType().equals(Type.DOUBLE)) {
                    entityProps.put(mName, new EntityProperty((Double) inputRecord.get(f.pos())));
                } else if (fSchema.getType().equals(Type.INT)) {
                    entityProps.put(mName, new EntityProperty((Integer) inputRecord.get(f.pos())));
                } else if (fSchema.getType().equals(Type.BYTES)) {
                    entityProps.put(mName, new EntityProperty((byte[]) inputRecord.get(f.pos())));
                }
                //
                else if (fSchema.getType().equals(Type.LONG)) {
                    String clazz = fSchema.getProp(SchemaConstants.JAVA_CLASS_FLAG);
                    if (clazz != null && clazz.equals(Date.class.getCanonicalName())) {
                        Date dt = null;
                        try {
                            dt = new SimpleDateFormat(fSchema.getProp(SchemaConstants.TALEND_COLUMN_PATTERN))
                                    .parse(inputRecord.get(f.pos()).toString());
                            entityProps.put(mName, new EntityProperty(dt));
                        } catch (ParseException e) {
                            LOGGER.error("Error while parsing date : {}", e);
                            if (properties.dieOnError.getValue()) {
                                throw new ComponentException(e);
                            }
                        }
                    } else
                        entityProps.put(mName, new EntityProperty((Long) inputRecord.get(f.pos())));
                }
                //
                else if (fSchema.getType().equals(Type.STRING)) {
                    entityProps.put(mName, new EntityProperty((String) inputRecord.get(f.pos())));
                } else { // use string as default type...
                    entityProps.put(mName, new EntityProperty((String) inputRecord.get(f.pos())));
                }
            }
        }
        // Etag is needed for some operations (delete, merge, replace) but we rely only on PK and RK for those ones.
        entity.setEtag("*");
        entity.setProperties(entityProps);

        if (processOperationInBatch) {
            addOperationToBatch(entity, inputRecord);

        } else {
            processSingleOperation(entity);
        }
    }

    @Override
    public Result close() throws IOException {
        if (batchOperationsCount > 0) {
            LOGGER.debug("{} operation(s) remaining in batch queue, executing batch.", batchOperationsCount);
            processBatch();
        }
        table = null;
        client = null;
        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return this.writeOperation;
    }

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }

    private IndexedRecordConverter<IndexedRecord, IndexedRecord> getFactory(Object datum) {
        if (factory == null) {
            factory = new GenericIndexedRecordConverter();
            factory.setSchema(writeSchema);
        }
        return factory;
    }

    private TableOperation getTableOperation(DynamicTableEntity entity) {
        TableOperation tableOpe = null;
        switch (actionData) {
        case Insert:
            tableOpe = TableOperation.insert(entity);
            break;
        case Insert_Or_Merge:
            tableOpe = TableOperation.insertOrMerge(entity);
            break;
        case Insert_Or_Replace:
            tableOpe = TableOperation.insertOrReplace(entity);
            break;
        case Merge:
            tableOpe = TableOperation.merge(entity);
            break;
        case Replace:
            tableOpe = TableOperation.replace(entity);
            break;
        case Delete:
            tableOpe = TableOperation.delete(entity);
            break;
        default:
            LOGGER.error("No specified operation for table");
        }

        return tableOpe;
    }

    private void processSingleOperation(DynamicTableEntity entity) throws IOException {
        try {
            TableOperation ope = getTableOperation(entity);
            table.execute(ope);
            handleSuccess(inputRecord, 1);
        } catch (StorageException e) {
            LOGGER.error("processSingleOperation::" + actionData + " : " + e.getLocalizedMessage());

            if (properties.dieOnError.getValue()) {
                throw new ComponentException(e);
            }
            handleReject(inputRecord, e, 1);
        }
    }

    private void addOperationToBatch(DynamicTableEntity entity, IndexedRecord record) throws IOException {
        if (latestPartitionKey == null || latestPartitionKey.isEmpty()) {
            latestPartitionKey = entity.getPartitionKey();
        }
        // we reached the threshold for batch OR changed PartitionKey
        if (batchOperationsCount == 100 || !entity.getPartitionKey().equals(latestPartitionKey)) {
            processBatch();
            latestPartitionKey = entity.getPartitionKey();
        }
        TableOperation to = getTableOperation(entity);
        batchOperations.add(to);
        batchRecords.add(record);
        batchOperationsCount++;
        latestPartitionKey = entity.getPartitionKey();
    }

    private void processBatch() throws IOException {
        TableBatchOperation batch = new TableBatchOperation();
        batch.addAll(batchOperations);
        //
        try {
            table.execute(batch);

            handleSuccess(null, batchOperationsCount);

        } catch (StorageException e) {
            LOGGER.error("processBatch::" + actionData + " : " + e.getLocalizedMessage());

            handleReject(null, e, batchOperationsCount);

            if (properties.dieOnError.getValue()) {
                throw new ComponentException(e);
            }
        }
        // reset operations, count and marker
        batchOperations.clear();
        batchRecords.clear();
        batchOperationsCount = 0;
        latestPartitionKey = "";
    }

    private void handleSuccess(IndexedRecord record, int counted) {
        result.successCount = result.successCount + counted;
        if (writeSchema == null || writeSchema.getFields().isEmpty())
            return;
        if (record != null) {
            successfulWrites.add(record);
        } else {
            successfulWrites.addAll(batchRecords);
        }
    }

    private void handleReject(IndexedRecord record, StorageException e, int counted) {
        result.rejectCount = result.rejectCount + counted;
        Schema rejectSchema = properties.schemaReject.schema.getValue();
        if (rejectSchema == null || rejectSchema.getFields().isEmpty()) {
            LOGGER.warn("handleReject: Not reject schema defined!");
            return;
        }
        if (record != null && record.getSchema().equals(rejectSchema)) {
            rejectedWrites.add(record);
        } else {
            if (processOperationInBatch) {
                for (IndexedRecord r : batchRecords) {
                    IndexedRecord reject = new GenericData.Record(rejectSchema);
                    reject.put(rejectSchema.getField("errorCode").pos(), e.getErrorCode());
                    reject.put(rejectSchema.getField("errorMessage").pos(), e.getMessage());
                    for (Schema.Field outField : reject.getSchema().getFields()) {
                        Object outValue;
                        Schema.Field inField = r.getSchema().getField(outField.name());
                        if (inField != null) {
                            outValue = r.get(inField.pos());
                            reject.put(outField.pos(), outValue);
                        }
                    }
                    rejectedWrites.add(reject);
                }

            } else {
                IndexedRecord reject = new GenericData.Record(rejectSchema);
                reject.put(rejectSchema.getField("errorCode").pos(), e.getErrorCode());
                reject.put(rejectSchema.getField("errorMessage").pos(), e.getMessage());
                for (Schema.Field outField : reject.getSchema().getFields()) {
                    Object outValue;
                    Schema.Field inField = record.getSchema().getField(outField.name());
                    if (inField != null) {
                        outValue = record.get(inField.pos());
                        reject.put(outField.pos(), outValue);
                    }
                }
                rejectedWrites.add(reject);
            }
        }
    }
}
