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
package org.talend.components.splunk.runtime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.FieldBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.runtime.GenericAvroRegistry;
import org.talend.components.splunk.connection.TSplunkEventCollectorConnection;
import org.talend.components.splunk.objects.SplunkJSONEvent;
import org.talend.components.splunk.objects.SplunkJSONEventBuilder;
import org.talend.components.splunk.objects.SplunkJSONEventField;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class TSplunkEventCollectorWriter implements Writer<Result> {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(TSplunkEventCollectorWriter.class);

    private static final String servicesSuffix = "services/collector";

    private TSplunkEventCollectorWriteOperation writeOperation;

    private String fullRequestUrl;

    private String token;

    private String uid;

    private int eventsBatchSize;

    private TSplunkEventCollectorConnection splunkConnection;

    private IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    private int dataCount;

    private int successCount;

    private int rejectCount;

    private int lastErrorCode;

    private String lastErrorMessage;

    private List<SplunkJSONEvent> splunkObjectsForBulk;

    private I18nMessages messageFormatter;

    private AtomicBoolean closed = new AtomicBoolean(false);

    private Schema defaultSchema;

    public TSplunkEventCollectorWriter(TSplunkEventCollectorWriteOperation writeOperation, String serverUrl, String token,
            int eventsBatchSize, Schema designSchema, RuntimeContainer container) {
        this.writeOperation = writeOperation;
        this.fullRequestUrl = serverUrl.endsWith("/") ? (serverUrl + servicesSuffix) : (serverUrl + "/" + servicesSuffix);
        this.token = token;
        this.eventsBatchSize = eventsBatchSize;
        setSchema(designSchema);
    }

    private void setSchema(Schema designSchema) {
        if (designSchema != null && AvroUtils.isIncludeAllFields(designSchema)) {
            this.defaultSchema = initDefaultSchema(designSchema);
        } else {
            this.defaultSchema = designSchema;
        }
    }

    private Schema initDefaultSchema(Schema designSchema) {
        AvroRegistry avroReg = new AvroRegistry();
        FieldAssembler<Schema> record = SchemaBuilder.record("Main").fields();
        for (SplunkJSONEventField metadataField : SplunkJSONEventField.getMetadataFields()) {
            Schema base = avroReg.getConverter(metadataField.getDataType()).getSchema();
            FieldBuilder<Schema> fieldBuilder = record.name(metadataField.getName());
            if (metadataField.getName().equals(SplunkJSONEventField.TIME.getName())) {
                String datePattern;
                Field designField = designSchema.getField(metadataField.getName());
                if (designField != null) {
                    datePattern = designField.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
                } else {
                    datePattern = designSchema.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
                }
                if (datePattern == null || datePattern.isEmpty()) {
                    datePattern = "dd-MM-yyyy";
                }
                fieldBuilder.prop(SchemaConstants.TALEND_COLUMN_PATTERN, datePattern);
            }
            fieldBuilder.type(AvroUtils.wrapAsNullable(base)).noDefault();
        }
        Schema defaultSchema = record.endRecord();
        return defaultSchema;
    }

    @Override
    public void open(String uId) throws IOException {
        closed.set(false);
        this.uid = uId;
        if (splunkConnection == null) {
            splunkConnection = new TSplunkEventCollectorConnection();
            splunkConnection.connect();
        }
        if (splunkObjectsForBulk == null) {
            splunkObjectsForBulk = new ArrayList<>();
        }
    }

    @Override
    public void write(Object datum) throws IOException {
        if (datum == null) {
            return;
        } // else handle the data.
        IndexedRecord input = getFactory(datum).convertToAvro(datum);

        SplunkJSONEvent event = SplunkJSONEventBuilder.createEvent();

        for (Schema.Field f : input.getSchema().getFields()) {
            Schema.Field defaultField = null;
            if (defaultSchema != null) {
                defaultField = defaultSchema.getField(f.name());
            }
            Object inputValue = input.get(f.pos());
            if (defaultField != null && defaultField.name().equals(SplunkJSONEventField.TIME.getName()) && inputValue != null
                    && inputValue instanceof String) {
                Object value = GenericAvroRegistry.get().convertToString(defaultField).convertToAvro((String) inputValue);
                SplunkJSONEventBuilder.setField(event, f.name(), value, true);
            } else if (inputValue != null) {
                SplunkJSONEventBuilder.setField(event, f.name(), inputValue, true);
            }
        }
        LOGGER.debug("Added event to bulk queue." + String.valueOf(event));
        splunkObjectsForBulk.add(event);
        LOGGER.debug("Events bulk queue size " + splunkObjectsForBulk.size());
        if (splunkObjectsForBulk.size() >= eventsBatchSize) {
            doSend();
        }
    }

    private void doSend() throws IOException {
        if (splunkObjectsForBulk.isEmpty()) {
            return;
        }
        dataCount += splunkObjectsForBulk.size();
        HttpPost request = createRequest(splunkObjectsForBulk);

        HttpResponse response = splunkConnection.sendRequest(request);

        String jsonResponseString = EntityUtils.toString(response.getEntity());
        try {
            handleResponse(jsonResponseString);
        } catch (IOException e) {
            rejectCount += splunkObjectsForBulk.size();
            throw e;
        } finally {
            splunkObjectsForBulk.clear();
        }
    }

    private void handleResponse(String jsonResponseString) throws IOException {
        if (jsonResponseString == null || jsonResponseString.trim().isEmpty()) {
            throw new IOException(getMessage("error.emptyResponse"));
        }
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(jsonResponseString);
            LOGGER.debug("Response String:/r/n" + String.valueOf(json));
            lastErrorCode = ((Long) json.get("code")).intValue();
            lastErrorMessage = (String) json.get("text");
            if (lastErrorCode != 0) {
                throw new IOException(getMessage("error.codeMessage", lastErrorCode, lastErrorMessage));
            }
            successCount += splunkObjectsForBulk.size();
        } catch (ParseException e) {
            throw new IOException(getMessage("error.responseParseException", e.getMessage()));
        }
    }

    public HttpPost createRequest(List<SplunkJSONEvent> events) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(fullRequestUrl);
        request.addHeader("Authorization", "Splunk " + token);
        StringBuffer requestString = new StringBuffer();
        for (SplunkJSONEvent event : events) {
            requestString.append(event.toString());
        }
        request.setEntity(new StringEntity(requestString.toString()));
        return request;
    }

    public IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return factory;
    }

    @Override
    public Result close() throws IOException {
        if (closed.getAndSet(true)) {
            LOGGER.debug("Already Closed.");
        } else {
            LOGGER.debug("Closing.");
            LOGGER.debug("Sending " + splunkObjectsForBulk.size() + " elements left in queue.");
            try {
                if (splunkObjectsForBulk != null) {
                    doSend();
                }
            } finally {
                releaseResources();
                LOGGER.debug("Closed.");
            }
        }
        return new SplunkWriterResult(uid, dataCount, successCount, rejectCount, lastErrorCode, lastErrorMessage);
    }

    private void releaseResources() {
        if (splunkConnection != null) {
            splunkConnection.close();
            splunkConnection = null;
        }
        if (splunkObjectsForBulk != null) {
            splunkObjectsForBulk.clear();
            splunkObjectsForBulk = null;
        }
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    private String getMessage(String key, Object... arguments) {
        if (messageFormatter == null) {
            messageFormatter = GlobalI18N.getI18nMessageProvider().getI18nMessages(this.getClass());
        }
        return messageFormatter.getMessage(key, arguments);
    }

}
