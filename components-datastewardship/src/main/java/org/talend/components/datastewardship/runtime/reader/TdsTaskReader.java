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
package org.talend.components.datastewardship.runtime.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.joda.time.Instant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.talend.components.datastewardship.avro.TaskAdapterFactory;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.common.TdsUtils;
import org.talend.components.datastewardship.runtime.TdsSource;
import org.talend.components.datastewardship.runtime.TdsTaskSource;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class TdsTaskReader extends TdsReader {

    /**
     * IndexedRecord converter
     */
    private IndexedRecordConverter<JSONObject, IndexedRecord> factory;

    /**
     * Stores HTTP parameters which are shared between requests
     */
    private final Map<String, Object> sharedParameters;

    /**
     * JSON string, which represents result obtained from TDS server
     */
    private String response;

    /**
     * A List of tasks with fields defined in schema
     */
    private List<JSONObject> taskList;

    /**
     * Index of current Task
     */
    private int taskIndex = 0;

    /**
     * Page size
     */
    private int size = 50;

    /**
     * Page number
     */
    private int page = 0;

    /**
     * @param source
     */
    public TdsTaskReader(TdsSource source) {
        super(source);
        factory = new TaskAdapterFactory();
        factory.setSchema(source.getSchema());
        size = getCurrentSource().getBatchSize();
        sharedParameters = createSharedParameters();
    }

    @Override
    public boolean start() throws IOException {
        started = true;
        queryTasks();
        return hasMoreRecords;
    }

    @Override
    public boolean advance() throws IOException {
        if (!started) {
            throw new IOException("Reader wasn't started");
        }
        taskIndex++;

        if (taskIndex < taskList.size()) {
            return true;
        } else {
            hasMoreRecords = false;
            if (taskList.size() == size) {
                page++;
                queryTasks();
            }
        }
        return hasMoreRecords;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (!started) {
            throw new NoSuchElementException("Reader wasn't started");
        }
        if (!hasMoreRecords) {
            throw new NoSuchElementException("No records available");
        }
        JSONObject task = taskList.get(taskIndex);
        return factory.convertToAvro(task);
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        if (!started) {
            throw new NoSuchElementException("Reader wasn't started");
        }
        if (!hasMoreRecords) {
            throw new NoSuchElementException("No records available");
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public TdsTaskSource getCurrentSource() {
        return (TdsTaskSource) source;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

    /**
     * Creates and returns map with shared http query parameters
     * 
     * @return shared http parameters
     */
    private Map<String, Object> createSharedParameters() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        String query = getCurrentSource().getSearchQuery();
        if (query != null && !query.trim().isEmpty()) {
            parameters.put("query", query); //$NON-NLS-1$
        }
        parameters.put("size", getCurrentSource().getBatchSize()); //$NON-NLS-1$
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Makes http request to the server and process its response
     * 
     * @throws IOException in case of exception during http connection
     */
    protected void queryTasks() throws IOException {
        Map<String, Object> parameters = new HashMap<String, Object>(sharedParameters);
        parameters.put("page", page); //$NON-NLS-1$
        String taskResource = TdsUtils.getTaskQueryResource(getCurrentSource().getCampaignName(),
                getCurrentSource().getTaskState());
        response = tdsConn.get(taskResource, parameters);
        taskList = processResponse(response);
        if (!taskList.isEmpty()) {
            hasMoreRecords = true;
            taskIndex = 0;
            result.totalCount = result.totalCount + taskList.size();
        }
    }

    private List<JSONObject> processResponse(String response) throws IOException {
        List<JSONObject> tasks = new ArrayList<JSONObject>();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray taskArray = (JSONArray) jsonParser.parse(response);
            JSONObject rawJson = null;
            for (Object obj : taskArray) {
                rawJson = (JSONObject) obj;
                tasks.addAll(parseTaskList(rawJson));
            }
        } catch (ParseException e) {
            throw new IOException("Response could not be parsed.", e);
        }
        return tasks;
    }

    @SuppressWarnings("unchecked")
    private List<JSONObject> parseTaskList(JSONObject rawJson) {
        List<JSONObject> taskList = new ArrayList<JSONObject>();
        
        // Golden record
        JSONObject goldenRecord = (JSONObject) rawJson.get("record"); //$NON-NLS-1$
        JSONObject goldenTask = new JSONObject();
        for (Schema.Field f : getCurrentSource().getSchema().getFields()) {
            String nameInSchema = f.name();
            if (TdsUtils.isMasterField(nameInSchema)) {
                goldenTask.put(nameInSchema, true);
            } else if (TdsUtils.isSourceField(nameInSchema)) {
                goldenTask.put(nameInSchema, null);
            } else if (TdsUtils.isMetadataField(nameInSchema)) {
                String nameInJson = TdsUtils.getNameInJson(nameInSchema);
                if (nameInJson != null) {
                    goldenTask.put(nameInSchema, rawJson.get(nameInJson));
                }
            } else {
                goldenTask.put(nameInSchema, goldenRecord.get(nameInSchema));
            }
        }
        taskList.add(goldenTask);
        
        // Source records
        if (CampaignType.MERGING == getCurrentSource().getCampaignType() && !getCurrentSource().isGoldenOnly()) {
            JSONArray sourceRecords = (JSONArray) rawJson.get("sourceRecords"); //$NON-NLS-1$
            for (Object obj : sourceRecords) {
                JSONObject sourceRawJson = (JSONObject) obj;
                JSONObject sourceTask = new JSONObject();
                JSONObject sourceRecord = (JSONObject) sourceRawJson.get("record"); //$NON-NLS-1$
                for (Schema.Field f : getCurrentSource().getSchema().getFields()) {
                    String nameInSchema = f.name();
                    if (TdsUtils.isMasterField(nameInSchema)) {
                        sourceTask.put(nameInSchema, false);
                    } else if (TdsUtils.isSourceField(nameInSchema)) {
                        sourceTask.put(nameInSchema, sourceRawJson.get("source")); //$NON-NLS-1$
                    } else if (TdsUtils.isMetadataField(nameInSchema)) {
                        String nameInJson = TdsUtils.getNameInJson(nameInSchema);
                        if (nameInJson != null) {
                            sourceTask.put(nameInSchema, rawJson.get(nameInJson));// same with golden record
                        }
                    } else {
                        sourceTask.put(nameInSchema, sourceRecord.get(nameInSchema));
                    }
                }
                taskList.add(sourceTask);
            }
        }
        return taskList;
    }
}
