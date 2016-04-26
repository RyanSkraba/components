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

import org.apache.avro.Schema;
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
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.splunk.TSplunkEventCollectorProperties;
import org.talend.components.splunk.connection.TSplunkEventCollectorConnection;
import org.talend.components.splunk.objects.SplunkJSONEvent;
import org.talend.components.splunk.objects.SplunkJSONEventBuilder;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;


/**
 * created by dmytro.chmyga on Apr 25, 2016
 */
public class TSplunkEventCollectorWriter implements Writer<WriterResult> {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(TSplunkEventCollectorWriter.class);
    
    private TSplunkEventCollectorWriteOperation writeOperation;
    private String serverUrl;
    private String token;
    private String uid;
    private int eventsBatchSize;
    private TSplunkEventCollectorConnection splunkConnection;
    private IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;
    private int dataCount;
    
    private int lastErrorCode;
    private String lastErrorMessage;
    
    private final RuntimeContainer container;
    
    private List<SplunkJSONEvent> splunkObjectsForBulk;
    
    public TSplunkEventCollectorWriter(TSplunkEventCollectorWriteOperation writeOperation, String serverUrl, String token, int eventsBatchSize,
                                        RuntimeContainer container) {
        this.writeOperation = writeOperation;
        this.serverUrl = serverUrl;
        this.token = token;
        this.eventsBatchSize = eventsBatchSize;
        this.container = container;
    }
    
    @Override
    public void open(String uId) throws IOException {
        this.uid = uId;
        if(splunkConnection == null) {
            splunkConnection = new TSplunkEventCollectorConnection();
            splunkConnection.connect();
        }
        if(splunkObjectsForBulk == null) {
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
            if (input.get(f.pos()) != null) {
                SplunkJSONEventBuilder.setField(event, f.name(), input.get(f.pos()), true);
            }
        }
        LOGGER.debug("Added event to bulk queue." + String.valueOf(event));
        splunkObjectsForBulk.add(event);
        LOGGER.debug("Events bulk queue size " + splunkObjectsForBulk);
        if(splunkObjectsForBulk.size() >= eventsBatchSize) {
            doSend();
        }
    }
    
    private void doSend() throws IOException {
        if(splunkObjectsForBulk.isEmpty()) {
            return;
        }
        HttpPost request = createRequest(splunkObjectsForBulk);
        
        HttpResponse response = splunkConnection.sendRequest(request);
        
        String jsonResponseString = EntityUtils.toString(response.getEntity());
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse(jsonResponseString);
            dataCount += splunkObjectsForBulk.size();
            LOGGER.debug("Response String:/r/n" + String.valueOf(json)); 
            lastErrorCode = ((Long)json.get("code")).intValue();
            lastErrorMessage = (String)json.get("text");
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            splunkObjectsForBulk.clear();
        }
        
    }
    
    public HttpPost createRequest(List<SplunkJSONEvent> events) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(serverUrl + "/services/collector");
        request.addHeader("Authorization", "Splunk " + token);
        StringBuffer requestString = new StringBuffer();
        for(SplunkJSONEvent event : events) {
            requestString.append(event.toString());
        }
        request.setEntity(new StringEntity(requestString.toString()));
        return request;
    }

    public IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordAdapterFactory<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createAdapterFactory(datum.getClass());
        }
        return factory;
    }

    @Override
    public WriterResult close() throws IOException {
        doSend();
        container.setComponentData(container.getCurrentComponentId(), "_" + TSplunkEventCollectorProperties.RESPONSE_CODE_NAME, lastErrorCode);
        container.setComponentData(container.getCurrentComponentId(), "_" + TSplunkEventCollectorProperties.ERROR_MESSAGE_NAME, lastErrorMessage);
        splunkConnection.close();
        splunkConnection = null;
        splunkObjectsForBulk.clear();
        splunkObjectsForBulk = null;
        return new WriterResult(uid, dataCount);
    }

    @Override
    public WriteOperation<WriterResult> getWriteOperation() {
        return writeOperation;
    }

}
