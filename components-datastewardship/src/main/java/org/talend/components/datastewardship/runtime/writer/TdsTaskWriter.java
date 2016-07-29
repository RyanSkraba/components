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
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.datastewardship.runtime.TdsTaskSink;
import org.talend.components.datastewardship.runtime.TdsTaskWriteOperation;

/**
 * TDS Task {@link Writer}
 */
public class TdsTaskWriter extends TdsWriter {

    /**
     * Schema retrieved from incoming data
     */
    private Schema dataSchema;

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
        
        TdsTaskSink sink = getWriteOperation().getSink();

        IndexedRecord record = getFactory(datum).convertToAvro(datum);
        if (dataSchema == null) {
            dataSchema = record.getSchema();
        }

        JSONArray taskObjs = new JSONArray();
        Map<String, Object> taskObj = new HashMap<String, Object>();
        Map<String, Object> recordObj = new HashMap<String, Object>();
        for (Schema.Field f : dataSchema.getFields()) {
            Object value = record.get(f.pos());
            recordObj.put(f.name(), value);
        }
        taskObj.put("type", sink.getCampaignType()); //$NON-NLS-1$
        taskObj.put("record", recordObj); //$NON-NLS-1$
        taskObjs.add(taskObj);

        String resourceToCreate = "api/v1/campaigns/owned/" + sink.getCampaignName() + "/tasks"; //$NON-NLS-1$ //$NON-NLS-2$

        int statusCode = getConnection().post(resourceToCreate, taskObjs.toJSONString());
        handleResponse(statusCode, resourceToCreate, record);
    }
}
