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
package org.talend.components.jdbc.runtime.writer;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.avro.JDBCAvroRegistry;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.avro.JDBCSPIndexedRecordCreator;
import org.talend.components.jdbc.runtime.JDBCSPSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * the JDBC writer for JDBC SP
 *
 */
public class JDBCSPWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private transient static final Logger LOG = LoggerFactory.getLogger(JDBCSPWriter.class);

    private WriteOperation<Result> writeOperation;

    private Connection conn;

    private JDBCSPSink sink;

    private AllSetting setting;

    private RuntimeContainer runtime;

    private Result result;

    private boolean useExistedConnection;

    private CallableStatement cs;

    private final List<IndexedRecord> successfulWrites = new ArrayList<>();

    private final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private Schema componentSchema;

    private Schema outputSchema;

    public JDBCSPWriter(WriteOperation<Result> writeOperation, RuntimeContainer runtime) {
        this.writeOperation = writeOperation;
        this.runtime = runtime;
        sink = (JDBCSPSink) writeOperation.getSink();
        setting = sink.properties.getRuntimeSetting();

        useExistedConnection = setting.getReferencedComponentId() != null;

        result = new Result();

        componentSchema = CommonUtils.getMainSchemaFromInputConnector((ComponentProperties) sink.properties);
        outputSchema = CommonUtils.getOutputSchema((ComponentProperties) sink.properties);
    }

    public void open(String uId) throws IOException {
        try {
            conn = sink.getConnection(runtime);
            cs = conn.prepareCall(sink.getSPStatement(setting));
        } catch (SQLException | ClassNotFoundException e) {
            throw CommonUtils.newComponentException(e);
        }
    }

    private JDBCSPIndexedRecordCreator indexedRecordCreator;

    public void write(Object datum) throws IOException {
        result.totalCount++;

        cleanWrites();
        
        IndexedRecord inputRecord = this.getGenericIndexedRecordConverter(datum).convertToAvro(datum);

        Schema inputSchema = inputRecord.getSchema();

        try {

            sink.fillParameters(cs, componentSchema, inputSchema, inputRecord, setting);

            cs.execute();

            if (indexedRecordCreator == null) {
                indexedRecordCreator = new JDBCSPIndexedRecordCreator();
                indexedRecordCreator.init(componentSchema, outputSchema, setting);
            }

            IndexedRecord outputRecord = indexedRecordCreator.createOutputIndexedRecord(cs, inputRecord);

            successfulWrites.add(outputRecord);
        } catch (Exception e) {
            throw CommonUtils.newComponentException(e);
        }

    }

    @Override
    public Result close() throws IOException {
        closeStatementQuietly(cs);

        closeAtLast();

        return result;
    }

    private void closeAtLast() {
        if (useExistedConnection) {
            return;
        }

        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            throw CommonUtils.newComponentException(e);
        }
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    // the converter convert all the data type to indexed record, in this class, it factly only convert the indexed record to
    // indexed record, not sure it's more than write like this :
    // (IndexedRecord)object
    private IndexedRecordConverter<Object, ? extends IndexedRecord> genericIndexedRecordConverter;

    @SuppressWarnings("unchecked")
    private IndexedRecordConverter<Object, ? extends IndexedRecord> getGenericIndexedRecordConverter(Object datum) {
        if (null == genericIndexedRecordConverter) {
            genericIndexedRecordConverter = (IndexedRecordConverter<Object, ? extends IndexedRecord>) JDBCAvroRegistry.get()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return genericIndexedRecordConverter;
    }

    private void closeStatementQuietly(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // close quietly
            }
        }
    }

    @Override
    public List<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public List<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }
    
    @Override
    public void cleanWrites() {
        successfulWrites.clear();
        rejectedWrites.clear();
    }

}
