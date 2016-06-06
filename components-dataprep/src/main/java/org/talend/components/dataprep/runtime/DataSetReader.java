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
package org.talend.components.dataprep.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.dataprep.connection.Column;
import org.talend.components.dataprep.connection.DataPrepConnectionHandler;
import org.talend.components.dataprep.connection.DataPrepField;
import org.talend.components.dataprep.connection.DataPrepStreamMapper;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of a reader.
 */
public class DataSetReader extends AbstractBoundedReader<IndexedRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetReader.class);

    private List<Column> sourceSchema;

    private Schema schema;

    private DataPrepConnectionHandler connectionHandler;

    private DataPrepStreamMapper dataPrepStreamMapper;

    private DataPrepAdaptorFactory adaptorFactory;

    private Result result;

    public DataSetReader(RuntimeContainer container, BoundedSource source, DataPrepConnectionHandler connectionHandler,
            Schema schema) {
        super(container, source);
        this.connectionHandler = connectionHandler;
        this.schema = schema;
        result = new Result();
    }

    @Override
    public boolean start() throws IOException {
        connectionHandler.connect();
        sourceSchema = connectionHandler.readSourceSchema();
        LOGGER.debug("Schema of data set : {}", sourceSchema);
        dataPrepStreamMapper = connectionHandler.readDataSetIterator();
        return dataPrepStreamMapper.initIterator();
    }

    @Override
    public boolean advance() throws IOException {
        result.totalCount++;
        return dataPrepStreamMapper.hasNextRecord();
    }

    @Override
    public IndexedRecord getCurrent() {
        Map<String, String> recordMap = dataPrepStreamMapper.nextRecord();
        LOGGER.debug("Record from data set: {}", recordMap);
        DataPrepField[] record = new DataPrepField[sourceSchema.size()];
        int i = 0;
        for (Column column : sourceSchema) {
            record[i] = new DataPrepField(column.getName(), column.getType(), recordMap.get(column.getId()));
            i++;
        }
        return ((DataPrepAdaptorFactory) getFactory()).convertToAvro(record);
    }

    @Override
    public void close() throws IOException {
        sourceSchema = null;
        dataPrepStreamMapper.close();
        connectionHandler.logout();
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

    private IndexedRecordConverter<?, IndexedRecord> getFactory() {
        if (adaptorFactory == null) {
            adaptorFactory = new DataPrepAdaptorFactory();
            adaptorFactory.setSchema(schema);
        }
        return adaptorFactory;
    }
}