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
package org.talend.components.dataprep;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;

@Ignore
public class TDataPrepConnectionHandlerTest {

    private static final String URL = "http://10.42.10.60:8888";
    private static final String LOGIN = "vincent@dataprep.com"; //"maksym@dataprep.com";
    private static final String PASS = "vincent"; //"maksym";
    private String string;

    @Test
    @Ignore
    public void validate() throws IOException {
        DataPrepConnectionHandler connectionHandler =
                new DataPrepConnectionHandler(URL, LOGIN, PASS, "sldfjsl");
        assertTrue(connectionHandler.validate());
    }

    @Test
    public void readSchema() throws IOException {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
                URL, LOGIN, PASS, "0d3df0df4a4aca0529ef5755bd03519adb115248");
        System.out.println(connectionHandler.connect());
        for (Column column: connectionHandler.readSourceSchema()) {
            System.out.println(column);
        }
        connectionHandler.logout();
    }

//    @Test
//    public void readDataSchema() throws IOException {
//        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
//                URL, LOGIN, PASS, "7291aa08-fed5-4267-83f5-ede7e7d66a72");
//        System.out.println(connectionHandler.connect());
//        for (Map<String,String> record: connectionHandler.readDataSet()) {
//            for (Map.Entry<String,String> field: record.entrySet())
//                System.out.println(field.getValue());
//            System.out.println("\n");
//        }
//        connectionHandler.logout();
//    }

    @Test
    public void logout() throws IOException {
        Request.Post(URL+"/logout").addHeader("Authorization","Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZW1vdGVTZXNzaW9uSWQiOiI5MzI0NjhiZS1mMWVhLTQ2YzctYTBhMC1jZTgyZWFhYWU4OWIiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiREFUQV9DVVJBVE9SIiwiREFUQV9TQ0lFTlRJU1QiXSwiaXNzIjoiZGF0YS1wcmVwIiwiZXhwIjoxNDYxODUwMzI2LCJpYXQiOjE0NjE4NDY3MjYsInVzZXJJZCI6InZpbmNlbnRAZGF0YXByZXAuY29tIiwianRpIjoiNThmODY1OWQtOWRjOC00YTUyLTk5ZmUtMTNiOTU0MTgzMjhhIn0.k14tGLc0mKPX73WAdfZSBQO8Ac47yRxF1HmQUMNS2XI").execute();
    }

    @Test
    public void schema() throws IOException {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
                URL, LOGIN, PASS, "db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        HttpResponse response = connectionHandler.connect();
        Header httpHead = response.getFirstHeader("Authorization");
        Request request = Request.Get(URL +"/api/datasets/"+ "db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e" + "/metadata");
        request.addHeader(httpHead);
        System.out.println(request.execute().returnContent().asString());
    }

    @Test
    public void createWithName() throws Exception {
        String name = "test_from_vincent";
        String body = "col1, col2, col3\ntest1, test2, test3\ntest4, test5, test6";
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(URL, LOGIN, PASS, name);
        connectionHandler.connect();
        connectionHandler.create(body);
        connectionHandler.logout();
    }

    @Test
    public void readData() throws IOException {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
                URL, LOGIN, PASS, "db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        HttpResponse response = connectionHandler.connect();
        Header httpHead = response.getFirstHeader("Authorization");
        Request request = Request.Get(URL+ "/api/datasets/" + "should be fixed" + "?metadata=false").
                addHeader(httpHead);
        HttpResponse current = null;
        try {
            current = request.execute().returnResponse();
        } finally {
            logout();
        }
        string = current.getEntity().toString();
        System.out.println(string);
    }

    @Test
    public void newRowHandling() throws IOException {
        String inputData = string; //"records":[{"0000":"test1","0001":" test2","0002":" test3","tdpId":1},{"0000":"test4","0001":" test5","0002":" test6","tdpId":2}];
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        JsonParser jsonParser = new JsonFactory().
                createJsonParser(new BufferedInputStream(new ByteArrayInputStream(inputData.getBytes())));
        while (!jsonParser.isClosed() && jsonParser.getCurrentToken() != JsonToken.END_ARRAY) {
            JsonToken token;
            while ((token = jsonParser.nextToken()) != JsonToken.END_OBJECT) {
                System.out.println(token);
            }
        }
    }

    @Test
    public void sendDataWithStream() throws IOException {
        java.net.URL url = new java.net.URL("http://52.31.50.21:80/api/datasets?name=" + "setName1" + "&folderPath="+"folderName");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setDoOutput(true);

        String body = "col1, col2, col3\ntest1, test2, test3\ntest4, test5, test6";
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

        for (int i = 0; i < 100000; i++) {
            outputStream.writeBytes(body);
            outputStream.flush();
            System.out.println(i);
        }
        outputStream.close();

        System.out.println(connection.getResponseCode());
        System.out.println(connection.getResponseMessage());
    }

    @Test
    public void sendDataWithStreamApache() throws IOException{
//        WebClient.create(URL).path(path).accept(MediaType.APPLICATION_XML)
//                .query(typeParam, type).query("_type", "xml").get(Products.class);
    }

    @Test
    public void testLiveDataSet() throws Exception {

        RuntimeContainer container = null;

        TDataSetOutputProperties properties = new TDataSetOutputProperties("TDataSetOutProperties");
        properties.mode.setValue(TDataSetOutputProperties.LIVE_DATASET);
        properties.url.setValue("http://127.0.0.1:8080/receivers2/debug");

        TDataSetOutputSink sink = new TDataSetOutputSink();
        sink.initialize(container, properties);
        sink.validate(container);
        final TDataSetWriteOperation writeOperation = (TDataSetWriteOperation) sink.createWriteOperation();
        final Writer<WriterResult> writer = writeOperation.createWriter(container);
        writer.open("test live datasets");
        for (int i=0; i<50; i++) {
            writer.write(i+";test-i;"+ new Date().getTime());
        }
        writer.close();
    }
}
