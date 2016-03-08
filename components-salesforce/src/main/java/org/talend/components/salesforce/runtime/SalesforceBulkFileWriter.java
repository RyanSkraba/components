// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Prepare Data Files for bulk execution
 */
final class SalesforceBulkFileWriter implements Writer<WriterResult> {


    private SalesforceWriteOperation salesforceWriteOperation;


    private String uId;

    private SalesforceSink sink;

    private RuntimeContainer adaptor;

    private TSalesforceOutputBulkProperties sprops;

    private int dataCount;

    private transient IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;

    private transient Schema schema;

    /*
    * For CSV writer
    * */

    private java.io.Writer rawWriter;

    private PrintWriter pw;

    public static final int INITIAL_STRING_SIZE = 128;

    private char separator = ',';

    private char quotechar = '"';

    private char escapechar = '"';

    private String lineEnd;

    public enum QuoteStatus {
        FORCE,
        AUTO,
        NO
    }

    private QuoteStatus quotestatus = QuoteStatus.AUTO;

    /**
     * DOC jzhao SalesforceBulkFileWriter constructor comment.
     *
     * @param salesforceWriteOperation
     * @param adaptor
     */
    public SalesforceBulkFileWriter(SalesforceWriteOperation salesforceWriteOperation, RuntimeContainer adaptor) {
        this.salesforceWriteOperation = salesforceWriteOperation;
        this.adaptor = adaptor;
        sink = (SalesforceSink) salesforceWriteOperation.getSink();
        sprops = (TSalesforceOutputBulkProperties)(sink.getSalesforceOutputProperties());

    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;

//        Schema schema = RuntimeHelper.resolveSchema(adaptor, sink,
//                new Schema.Parser().parse(sprops.schema.schema.getStringValue()));

        schema = new Schema.Parser().parse(sprops.schema.schema.getStringValue());

        File file = new File(sprops.fileName.getStringValue());
        this.rawWriter = new BufferedWriter(new OutputStreamWriter(
                new java.io.FileOutputStream(file, false), "UTF-8"));
        this.pw = new PrintWriter(this.rawWriter);

        List<String> headers = new ArrayList<String>();
        for(Schema.Field f :schema.getFields()){
            headers.add(f.name());
        }
        writeNext(headers.toArray(new String[headers.size()]));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Object datum) throws IOException {
        // Ignore empty rows.
        if (null == datum) {
            return;
        }

        // This is all we need to do in order to ensure that we can process the incoming value as an IndexedRecord.
        if (null == factory) {
            factory = (IndexedRecordAdapterFactory<Object, ? extends IndexedRecord>) SalesforceAvroRegistry.get()
                    .createAdapterFactory(datum.getClass());
        }
        IndexedRecord input = factory.convertToAvro(datum);

        List<String> values = new ArrayList<String>();
        for (Schema.Field f : input.getSchema().getFields()) {
            values.add(String.valueOf(input.get(f.pos())));
        }
        writeNext(values.toArray(new String[values.size()]));
        dataCount++;
    }

    private StringBuilder escape(String field, boolean quote) {
        if (quote) {
            return processLine(field);
        } else if (escapechar!=quotechar) {
            return processLine2(field);
        }
        return null;
    }

    public void setLineEnd(String lineEnd) {
        this.lineEnd = lineEnd;
    }

    /**
     * escape when text quote
     * @param nextElement
     * @return
     */
    protected StringBuilder processLine(String nextElement) {
        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        for (int j = 0; j < nextElement.length(); j++) {
            char nextChar = nextElement.charAt(j);
            if (nextChar == quotechar) {
                sb.append(escapechar).append(nextChar);
            } else if (nextChar == escapechar) {
                sb.append(escapechar).append(nextChar);
            } else {
                sb.append(nextChar);
            }
        }

        return sb;
    }

    /**
     * escape when no text quote
     * @param nextElement
     * @return
     */
    protected StringBuilder processLine2(String nextElement) {
        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        for (int j = 0; j < nextElement.length(); j++) {
            char nextChar = nextElement.charAt(j);
            if (nextChar == escapechar) {
                sb.append(escapechar).append(nextChar);
            } else if (nextChar == separator) {
                sb.append(escapechar).append(nextChar);
            } else if(lineEnd==null && (nextChar=='\r' || nextChar=='\n')){
                sb.append(escapechar).append(nextChar);
            } else if(lineEnd!=null && lineEnd.indexOf(nextChar) > -1) {
                sb.append(escapechar).append(nextChar);
                //TODO how to escape char sequence that contain more than one char without text quote?
            } else {
                sb.append(nextChar);
            }
        }

        return sb;
    }

    private boolean needQuote(String field, int fieldIndex) {
        boolean need =  field.indexOf(quotechar) > -1
                || field.indexOf(separator) > -1
                || (lineEnd == null && (field.indexOf('\n') > -1 || field.indexOf('\r') > -1))
                || (lineEnd != null && field.indexOf(lineEnd) > -1)
                || (fieldIndex == 0 && field.length() == 0);

        if(!need && field.length() > 0) {
            char first = field.charAt(0);

            if (first == ' ' || first == '\t') {
                need = true;
            }

            if (!need && field.length() > 1) {
                char last = field.charAt(field.length() - 1);

                if (last == ' ' || last == '\t') {
                    need = true;
                }
            }
        }

        return need;
    }

    /**
     * Writes the next line to the file.
     *
     * @param nextLine a string array with each comma-separated element as a separate entry.
     */
    public void writeNext(String[] nextLine) {

        if (nextLine == null) {
            return;
        }

        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        for (int i = 0; i < nextLine.length; i++) {

            if (i != 0) {
                sb.append(separator);
            }

            String nextElement = nextLine[i];
            if (nextElement == null) {
                nextElement = "";
            }

            boolean quote = false;

            if(this.quotestatus == QuoteStatus.AUTO) {
                quote = needQuote(nextElement,i);
            } else if(this.quotestatus == QuoteStatus.FORCE) {
                quote = true;
            }

            if(quote) {
                sb.append(quotechar);
            }

            StringBuilder escapeResult = escape(nextElement,quote);
            if(escapeResult!=null) {
                sb.append(escapeResult);
            } else {
                sb.append(nextElement);
            }

            if(quote) {
                sb.append(quotechar);
            }
        }

        if(lineEnd!=null) {
            sb.append(lineEnd);
            pw.write(sb.toString());
        } else {
            pw.println(sb.toString());
        }

    }

    /**
     * Flush underlying stream to writer.
     *
     * @throws IOException if bad things happen
     */
    public void flush() throws IOException {

        pw.flush();

    }

    @Override
    public WriterResult close() throws IOException {
        flush();
        pw.close();
        rawWriter.close();
        // this should be computed according to the result of the write I guess but I don't know yet how exceptions are
        // handled by Beam.
        return new WriterResult(uId, dataCount);
    }

    @Override
    public WriteOperation<WriterResult> getWriteOperation() {
        return salesforceWriteOperation;
    }
}