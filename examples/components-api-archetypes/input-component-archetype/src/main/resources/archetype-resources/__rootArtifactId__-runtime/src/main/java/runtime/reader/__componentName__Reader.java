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
package ${package}.runtime.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.common.avro.RootSchemaUtils;
import org.talend.components.common.component.runtime.RootRecordUtils;
import ${package}.${componentPackage}.${componentName}Properties;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Simple implementation of a reader.
 */
public class ${componentName}Reader extends AbstractBoundedReader<IndexedRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(${componentName}Reader.class);

    private boolean started = false;
    
    private boolean hasMore = false;

    private BufferedReader reader = null;

    private IndexedRecord current;
    
    /**
     * Converts datum field values to avro format
     */
    private AvroConverter<String, IndexedRecord> converter;
    
    /**
     * Runtime schema - schema of data record
     */
    private Schema runtimeSchema;
    
    /**
     * Root schema includes Runtime schema and schema of out of band data (a.k.a flow variables)
     */
    private Schema rootSchema;
    
    /**
     * Holds values for return properties
     */
    private Result result;

    public ${componentName}Reader(${componentName}Source source) {
        super(source);
    }

    @Override
    public boolean start() throws IOException {
        reader = new BufferedReader(new FileReader(getCurrentSource().getFilePath()));
        result = new Result();
        LOGGER.debug("open: " + getCurrentSource().getFilePath()); //$NON-NLS-1$
        started = true;
        return advance();
    }

    @Override
    public boolean advance() throws IOException {
        if (!started) {
            throw new IllegalStateException("Reader wasn't started");
        }
        hasMore = reader.ready();
        if (hasMore) {
            String line = reader.readLine();
            // create the data schema
            Schema dataSchema = getRuntimeSchema(line);
            // create the data IndexRecord
            IndexedRecord dataRecord = getConverter(dataSchema).convertToAvro(line);
            // create the outOfBand record (since the schema is static)
            IndexedRecord outOfBandRecord = new GenericData.Record(${componentName}Properties.outOfBandSchema);
            outOfBandRecord.put(0, result.totalCount);
            // create the root record
            current = RootRecordUtils.createRootRecord(getRootSchema(), dataRecord, outOfBandRecord);
            result.totalCount++;
        }
        return hasMore;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (!started) {
            throw new NoSuchElementException("Reader wasn't started");
        }
        if (!hasMore) {
            throw new NoSuchElementException("Has no more elements");
        }
        return current;
    }

    @Override
    public void close() throws IOException {
        if (!started) {
            throw new IllegalStateException("Reader wasn't started");
        }
        reader.close();
        LOGGER.debug("close: " + getCurrentSource().getFilePath()); //$NON-NLS-1$
        reader = null;
        started = false;
        hasMore = false;
    }

    /**
     * Returns values of Return properties. It is called after component finished his work (after {@link this#close()} method)
     */
    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }
    
    @Override
    public ${componentName}Source getCurrentSource() {
        return (${componentName}Source) super.getCurrentSource();
    }

    /**
     * Returns implementation of {@link AvroConverter}, creates it if it doesn't
     * exist.
     * 
     * @param runtimeSchema
     *            Schema of data record
     * @return converter
     */
    private AvroConverter<String, IndexedRecord> getConverter(Schema runtimeSchema) {
        if (converter == null) {
            converter = getCurrentSource().createConverter(runtimeSchema);
        }
        return converter;
    }
    
    /**
     * Returns Runtime schema, which is used during data IndexedRecord creation
     * Creates the schema only once for all records
     * 
     * @param delimitedString delimited line, which was read from file
     * @return avro Runtime schema
     */
    private Schema getRuntimeSchema(String delimitedString) {
        if (runtimeSchema == null) {
            runtimeSchema = getCurrentSource().provideRuntimeSchema(delimitedString);
        }
        return runtimeSchema;
    }
    
    /**
     * Returns Root schema, which is used during IndexedRecord creation <br>
     * This should be called only after {@link this#getRuntimeSchema(String)} is
     * called
     * 
     * @return avro Root schema
     */
    private Schema getRootSchema() {
        if (rootSchema == null) {
            if (runtimeSchema == null) {
                throw new IllegalStateException("Runtime schema should be created before Root schema");
            } else {
                rootSchema = RootSchemaUtils.createRootSchema(runtimeSchema, ${componentName}Properties.outOfBandSchema);
            }
        }
        return rootSchema;
    }
}
