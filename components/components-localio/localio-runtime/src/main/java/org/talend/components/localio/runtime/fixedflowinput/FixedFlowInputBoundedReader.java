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
package org.talend.components.localio.runtime.fixedflowinput;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.io.BoundedSource.BoundedReader;

public class FixedFlowInputBoundedReader extends BoundedReader<IndexedRecord> {

    private String schemaString = "";

    private Schema schema = null;

    private DatumReader<IndexedRecord> reader = null;

    private String values = "";

    private int nbRows = 1;

    private int numberOfElement = 0;

    private FixedFlowInputBoundedSource source;

    private JsonDecoder decoder = null;

    private IndexedRecord currentRecord = null;

    public FixedFlowInputBoundedReader(FixedFlowInputBoundedSource source) {
        this.source = source;
    }

    /**
     * This method will instantiate correct Avro Schema object. This is mandatory since the "Schema" object of Avro are
     * not serializable.
     */
    public void deserializeSchema() {
        if (schema == null) {
            Schema.Parser parser = new Schema.Parser();
            schema = parser.parse(schemaString);
            reader = new GenericDatumReader<>(schema);
        }
    }

    @Override
    public BoundedSource<IndexedRecord> getCurrentSource() {
        return source;
    }

    @Override
    public boolean start() throws IOException {
        deserializeSchema();
        return advance();
    }

    @Override
    public boolean advance() throws IOException {
        if (numberOfElement >= nbRows){
            return false;
        }
        if (decoder == null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(values.getBytes());
            DataInputStream din = new DataInputStream(bais);
            decoder = DecoderFactory.get().jsonDecoder(schema, din);
        }
        try {
            currentRecord = reader.read(null, decoder);
            return true;
        } catch (EOFException e) {
            numberOfElement++;
            if (numberOfElement < nbRows) {
                ByteArrayInputStream bais = new ByteArrayInputStream(values.getBytes());
                DataInputStream din = new DataInputStream(bais);
                decoder = DecoderFactory.get().jsonDecoder(schema, din);
                currentRecord = reader.read(null, decoder);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return currentRecord;
    }

    @Override
    public void close() throws IOException {
    }

    public FixedFlowInputBoundedReader withSchema(String schemaString) {
        this.schemaString = schemaString.toString();
        return this;
    }

    public FixedFlowInputBoundedReader withValues(String values) {
        this.values = values;
        return this;
    }

    public FixedFlowInputBoundedReader withNbRows(int nbRows) {
        this.nbRows = nbRows;
        return this;
    }

}
