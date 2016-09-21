package org.talend.components.filedelimited.runtime;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.ComponentConstants;
import org.talend.components.filedelimited.tFileOutputDelimited.TFileOutputDelimitedProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.talend.csv.CSVWriter;

public class FileDelimitedWriter implements Writer<Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedWriter.class);

    private final FileDelimitedSink sink;

    private IndexedRecordConverter<IndexedRecord, IndexedRecord> factory;

    private int counter = 0;

    private boolean firstRow = true;

    private WriteOperation<Result> writeOperation;

    private final RuntimeContainer container;

    FileOutputDelimitedRuntime outputRuntime;

    TFileOutputDelimitedProperties props;

    private transient Schema recordSchema;

    private Result result;

    private CSVWriter csvWriter;

    private java.io.Writer writer;

    private int currentRowNo = 0;

    FileDelimitedWriter(FileDelimitedWriteOperation writeOperation, RuntimeContainer container) {
        this.writeOperation = writeOperation;
        this.container = container;
        sink = writeOperation.getSink();
        props = sink.getOutputProperties();
        outputRuntime = new FileOutputDelimitedRuntime(props);
    }

    @Override
    public void open(String uId) throws IOException {
        this.result = new Result(uId);
        if (recordSchema == null) {
            recordSchema = props.main.schema.getValue();
            if (AvroUtils.isIncludeAllFields(recordSchema)) {
                // if design schema include dynamic,need to get schema from record
                recordSchema = null;
            }
        }
        if (props.csvOptions.getValue()) {
            csvWriter = outputRuntime.getCsvWriter();

        } else {
            writer = outputRuntime.getWriter();
        }
        if (csvWriter != null) {
            outputRuntime.writeHeader(csvWriter, recordSchema);
        } else {
            outputRuntime.writeHeader(writer, recordSchema);
        }
    }

    @Override
    public void write(Object datum) throws IOException {
        counter++;
        if (datum == null) {
            return;
        }

        IndexedRecord inputRecord = getFactory(datum).convertToAvro((IndexedRecord) datum);
        if (recordSchema == null) {
            recordSchema = inputRecord.getSchema();
            if (csvWriter != null) {
                outputRuntime.writeHeader(csvWriter, recordSchema);
            } else {
                outputRuntime.writeHeader(writer, recordSchema);
            }
        }

        result.totalCount++;
        if (props.csvOptions.getValue()) {
            if (!props.targetIsStream.getValue() && props.split.getValue()) {
                if (currentRowNo != 0 && currentRowNo % props.splitEvery.getValue() == 0) {
                    csvWriter.close();
                    // close original outputStream
                    csvWriter = outputRuntime.getCsvWriter();
                    outputRuntime.writeHeader(csvWriter, recordSchema);
                }
                currentRowNo++;
            }
            csvWriter.writeNext(getValues(inputRecord));
            if (props.rowMode.getValue()) {
                outputRuntime.writer.write(outputRuntime.strWriter.getBuffer().toString());
                outputRuntime.strWriter.getBuffer().delete(0, outputRuntime.strWriter.getBuffer().length());
            }
            if (props.flushOnRow.getValue()) {

                if (result.getTotalCount() % props.flushOnRowNum.getValue() == 0) {
                    if (props.rowMode.getValue()) {
                        outputRuntime.writer.flush();
                    } else {
                        csvWriter.flush();
                    }
                }
            }

        } else {
            if (!props.targetIsStream.getValue() && props.split.getValue()) {
                if (!props.targetIsStream.getValue() && props.split.getValue()) {
                    writer.close();
                    // close original outputStream

                    writer = outputRuntime.getWriter();
                    outputRuntime.writeHeader(writer, recordSchema);
                }
                currentRowNo++;
            }
            writer.write(getRowString(inputRecord));
            if (props.flushOnRow.getValue()) {
                if (result.getTotalCount() % props.flushOnRowNum.getValue() == 0) {
                    writer.flush();
                }
            }
        }
        LOGGER.debug("Writing the record: " + result.totalCount);

    }

    @Override
    public Result close() throws IOException {
        if (props.csvOptions.getValue()) {
            if (props.targetIsStream.getValue()) {
                if (props.rowMode.getValue()) {
                    if (csvWriter != null) {
                        csvWriter.close();
                    }
                    if (outputRuntime.writer != null) {
                        outputRuntime.writer.flush();
                    }
                    if (outputRuntime.writer != null) {
                        outputRuntime.writer.flush();
                    }
                } else {

                }
            } else {
                if (csvWriter != null) {
                    csvWriter.close();
                }
                if (props.rowMode.getValue()) {
                    if (outputRuntime.writer != null) {
                        outputRuntime.writer.flush();
                        outputRuntime.writer.close();
                    }
                }
            }

        } else {
            if (props.targetIsStream.getValue()) {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } else {
                if (writer != null) {
                    writer.flush();
                    outputRuntime.streamWriter.flush();
                    writer.close();
                }
            }
        }

        result.successCount = result.totalCount;
        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    private IndexedRecordConverter<IndexedRecord, IndexedRecord> getFactory(Object datum) {
        if (factory == null) {
            factory = new FileDelimitedIndexedRecordConverter();
            recordSchema.addProp(ComponentConstants.FILE_ENCODING, outputRuntime.encoding);
            if (props.advancedSeparator.getValue()) {
                recordSchema.addProp(ComponentConstants.THOUSANDS_SEPARATOR, props.thousandsSeparator.getValue());
                recordSchema.addProp(ComponentConstants.DECIMAL_SEPARATOR, props.decimalSeparator.getValue());
            }
            factory.setSchema(recordSchema);
        }
        return factory;
    }

    private String[] getValues(IndexedRecord record) {
        String[] values = new String[recordSchema.getFields().size()];
        for (Schema.Field field : recordSchema.getFields()) {
            values[field.pos()] = String.valueOf(record.get(field.pos()));
        }
        return values;
    }

    private String getRowString(IndexedRecord record) {
        StringBuilder sb = new StringBuilder();
        for (Schema.Field field : recordSchema.getFields()) {
            sb.append(record.get(field.pos()));
            if (field.pos() != (recordSchema.getFields().size()-1)) {
                sb.append(outputRuntime.fieldSeparator);
            }
        }
        sb.append(outputRuntime.rowSeparator);
        return sb.toString();
    }

}
