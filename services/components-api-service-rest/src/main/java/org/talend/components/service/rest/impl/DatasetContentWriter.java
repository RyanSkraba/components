//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.impl;

import static java.lang.Integer.MAX_VALUE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.slf4j.Logger;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.java8.Consumer;

/**
 * Helps write AVRO records into an {@link OutputStream} in JSON or binary AVRO using the library tools.
 */
class DatasetContentWriter implements Function<DatasetRuntime<DatasetProperties<DatastoreProperties>>, Void> {

    private static final Logger log = getLogger(DatasetContentWriter.class);

    private final Integer limit;

    private final boolean json;

    private final OutputStream output;

    /**
     * @param limit the number of records to write
     * @param json  true to write JSon, false for binary Avro
     */
    DatasetContentWriter(OutputStream output, Integer limit, boolean json) {
        this.output = output;
        this.limit = limit;
        this.json = json;
    }

    @Override
    public Void apply(DatasetRuntime<DatasetProperties<DatastoreProperties>> dr) {
        try {
            final Encoder[] encoder = { null };
            dr.getSample(limit == null ? MAX_VALUE : limit, getWritingConsumer(encoder));
            if (encoder[0] != null)
                encoder[0].flush();
        } catch (RuntimeException | IOException e) {
            log.error("Couldn't create Avro records JSon encoder.", e);
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
        return null;
    }

    private Consumer<IndexedRecord> getWritingConsumer(Encoder[] encoder) {
        return new Consumer<IndexedRecord>() {

            GenericDatumWriter<IndexedRecord> writer = null;

            @Override
            public void accept(IndexedRecord ir) {
                if (writer == null) {
                    writer = new GenericDatumWriter<>(ir.getSchema());
                    try {
                        if (json) {
                            encoder[0] = EncoderFactory.get().jsonEncoder(ir.getSchema(), output);
                        } else {
                            encoder[0] = EncoderFactory.get().binaryEncoder(output, null);
                        }
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }

                }
                writeIndexedRecord(writer, encoder[0], ir);
            }
        };
    }

    private void writeIndexedRecord(GenericDatumWriter<IndexedRecord> writer, Encoder encoder, IndexedRecord indexedRecord) {
        try {
            writer.write(indexedRecord, encoder);
        } catch (IOException e) {
            log.warn("Couldn't serialize Avro record.", e);
        }
    }
}
