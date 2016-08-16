// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship.runtime.writer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.datastewardship.CampaignType;
import org.talend.components.datastewardship.connection.TdsConnection;
import org.talend.components.datastewardship.runtime.TdsTaskSink;
import org.talend.components.datastewardship.runtime.TdsTaskWriteOperation;

/**
 * TDS Task {@link Writer}
 */
public class TdsTaskWriter extends TdsWriter {
    
    private static final Logger LOG = LoggerFactory.getLogger(TdsTaskWriter.class);

    /**
     * Schema retrieved from incoming data
     */
    private Schema dataSchema;

    private String groupId;

    private JSONArray taskObjs = new JSONArray();

    private Map<String, Object> taskObj = new HashMap<String, Object>();

    private TdsTaskSink sink;

    private int batchSize = -1;

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation TDS {@link WriteOperation} instance
     */
    public TdsTaskWriter(TdsTaskWriteOperation writeOperation) {
        super(writeOperation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TdsTaskWriteOperation getWriteOperation() {
        return (TdsTaskWriteOperation) super.getWriteOperation();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void write(Object datum) throws IOException {
        if (!opened) {
            throw new IOException("Writer wasn't opened"); //$NON-NLS-1$
        }
        result.totalCount++;
        if (datum == null) {
            return;
        }

        sink = getWriteOperation().getSink();

        if (sink.getBatchSize() > 0) {
            batchSize = Integer.valueOf(sink.getBatchSize());
        }

        IndexedRecord record = getFactory(datum).convertToAvro(datum);
        if (dataSchema == null) {
            dataSchema = record.getSchema();
        }
        taskObj = new HashMap<String, Object>();
        if (sink.getCampaignType().equals(CampaignType.MERGING.toString())) {
            createMergingTasks(record);
        } else {
            Map<String, Object> recordObj = new HashMap<String, Object>();
            for (Schema.Field f : dataSchema.getFields()) {
                Object value = record.get(f.pos());
                recordObj.put(f.name(), value);
            }
            taskObj.put("type", sink.getCampaignType()); //$NON-NLS-1$
            taskObj.put("record", recordObj); //$NON-NLS-1$
            taskObjs.add(taskObj);
            if (taskObjs.size() == batchSize) {
                submit();
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void createMergingTasks(IndexedRecord record) throws IOException {
        Schema.Field groupIdField = dataSchema.getField(sink.getGroupIdColumn());
        Schema.Field sourceField = dataSchema.getField(sink.getSourceColumn());
        Schema.Field masterField = dataSchema.getField(sink.getMasterColumn());
        Schema.Field scoreField = dataSchema.getField(sink.getScoreColumn());

        JSONArray sourceRecords = new JSONArray();

        if (record.get(groupIdField.pos()).equals(groupId)) {
            // get the last taskObj
            taskObj = (Map<String, Object>) taskObjs.get(taskObjs.size() - 1);
            taskObjs.remove(taskObj);
            if (taskObj.get("sourceRecords") != null) { //$NON-NLS-1$
                sourceRecords = (JSONArray) taskObj.get("sourceRecords"); //$NON-NLS-1$
            }
        } else {
            // a new task
            if (groupId != null && taskObjs.size() == batchSize) {
                submit();
            }
            groupId = (String) record.get(groupIdField.pos());
            taskObj.put("type", CampaignType.MERGING.toString()); //$NON-NLS-1$
        }

        boolean master = (boolean) record.get(masterField.pos());
        Map<String, Object> recordObj = new HashMap<String, Object>();
        if (master) {
            // add master record
            for (Schema.Field f : dataSchema.getFields()) {
                if (!(f.equals(groupIdField) || f.equals(masterField) || f.equals(sourceField) || f.equals(scoreField))) {
                    Object value = record.get(f.pos());
                    recordObj.put(f.name(), value);
                }
            }
            taskObj.put("record", recordObj); //$NON-NLS-1$
        } else {
            // add source record
            String source = String.valueOf(record.get(sourceField.pos()));
            Map<String, Object> sourceRecordObj = new HashMap<String, Object>();
            for (Schema.Field f : dataSchema.getFields()) {
                if (!(f.equals(groupIdField) || f.equals(masterField) || f.equals(sourceField) || f.equals(scoreField))) {
                    Object value = record.get(f.pos());
                    recordObj.put(f.name(), value);
                }
                sourceRecordObj.put("source", source); //$NON-NLS-1$
                sourceRecordObj.put("record", recordObj); //$NON-NLS-1$
            }
            sourceRecords.add(sourceRecordObj);
            taskObj.put("sourceRecords", sourceRecords); //$NON-NLS-1$
        }
        taskObjs.add(taskObj);
    }

    @Override
    public Result close() throws IOException {
        if (taskObjs.size() > 0) {
            submit();
        }
        return super.close();
    }

    private void submit() throws IOException {
        String resourceToCreate = "api/" + TdsConnection.API_VERSION + "/campaigns/owned/" + sink.getCampaignName() + "/tasks"; //$NON-NLS-1$ //$NON-NLS-2$
        int statusCode = getConnection().post(resourceToCreate, taskObjs.toJSONString());
        handleResponse(statusCode, resourceToCreate, taskObjs.toJSONString());
        LOG.debug("Commit : " + taskObjs); //$NON-NLS-1$
        taskObjs.clear();
    }
}
