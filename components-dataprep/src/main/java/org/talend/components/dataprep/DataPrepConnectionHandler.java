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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class DataPrepConnectionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TDataSetInputDefinition.class);

    private final String url;

    private final String login;

    private final String pass;

    private final String mode;

    private final String dataSetName;

    private Header authorisationHeader;

    private static final int STATUS_OK = 200;

    DataPrepConnectionHandler(String url, String login, String pass, String mode, String dataSetName) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.mode = mode;
        this.dataSetName = dataSetName;
    }

    HttpResponse connect() throws IOException {
        Request request = Request.Post(url+"/login?username="+login+"&password="+pass);
        HttpResponse response = request.execute().returnResponse();
        authorisationHeader = response.getFirstHeader("Authorization");
        return response;
    }

    HttpResponse logout() throws IOException {
        Request request = Request.Post(url+"/logout").addHeader(authorisationHeader);
        return request.execute().returnResponse();
    }

    private int returnStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    boolean validate() throws IOException {
        int statusLogin = 0;
        int statusLogout = 0;
        statusLogin = returnStatusCode(connect());
        statusLogout = returnStatusCode(logout());
        if (statusLogin == STATUS_OK && statusLogout == STATUS_OK)
            return true;
        else
            return false;
    }

    List<Map<String,String>> readDataSet() throws IOException {
        Request request = Request.Get(url+ "/api/datasets/" + dataSetName + "?metadata=false").
                addHeader(authorisationHeader);
        HttpResponse current = null;
        try {
            current = request.execute().returnResponse();
        } finally {
            logout();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        SourceDataSet dataSet = objectMapper.readValue(current.getEntity().getContent(), SourceDataSet.class);
        return dataSet.getRecords();
    }

    void create(String data) throws IOException {
        int index = dataSetName.lastIndexOf("/");
        String setName = dataSetName.substring(index);
        String folderName = dataSetName.substring(0,index);

        Request request = Request.Post(url+"/api/datasets?name=" + setName + "&folderPath="+folderName);
        request.addHeader(authorisationHeader);
        request.bodyString(data ,ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8));
        request.execute();
    }

    List<Column> readSourceSchema() throws IOException {
        Request request = Request.Get(url +"/api/datasets/"+ dataSetName + "/metadata");
        request.addHeader(authorisationHeader);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String test = null;
        try {
            test = request.execute().returnContent().asString();
        } finally {
            logout();
        }
        MetaData metaData = objectMapper.readValue(test, MetaData.class);
        return metaData.getColumns();
    }

}
