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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DataPrepConnectionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TDataSetInputDefinition.class);

    private final String url;

    private final String login;

    private final String pass;

    private final String dataSetName;

    private Header authorisationHeader;

    private static final int STATUS_OK = 200;

    DataPrepConnectionHandler(String url, String login, String pass, String dataSetName) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.dataSetName = dataSetName;
    }

    HttpResponse connect() throws IOException {
        Request request = Request.Post(url+"/login?username="+login+"&password="+pass);
        HttpResponse response = request.execute().returnResponse();
        LOGGER.debug("Connect Response: " + response.toString());
        authorisationHeader = response.getFirstHeader("Authorization");
        return response;
    }

    HttpResponse logout() throws IOException {
        Request request = Request.Post(url+"/logout").addHeader(authorisationHeader);
        HttpResponse response = request.execute().returnResponse();
        LOGGER.debug("Logout Response: "+ response.toString());
        return response;
    }

    private int returnStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    boolean validate() throws IOException {
        int statusLogin;
        int statusLogout;
        statusLogin = returnStatusCode(connect());
        statusLogout = returnStatusCode(logout());
        if (statusLogin == STATUS_OK && statusLogout == STATUS_OK)
            return true;
        else
            return false;
    }

    DataPrepStreamMapper readDataSetIterator() throws IOException {
        Request request = Request.Get(url+ "/api/datasets/" + dataSetName + "?metadata=false").
                addHeader(authorisationHeader);
        HttpResponse current = request.execute().returnResponse();
        DataPrepStreamMapper dataPrepStreamMapper = new DataPrepStreamMapper(current.getEntity().getContent());

        LOGGER.debug("Read DataSet Response: {} ", current);

        return dataPrepStreamMapper;
    }

    void create(String data) throws IOException {
        int index = dataSetName.lastIndexOf("/");
        String setName = dataSetName.substring(index);
        String folderName = dataSetName.substring(0,index);
        LOGGER.debug("Folder Name: " + folderName+ "DataSet name: " + setName);

        Request request = Request.Post(url+"/api/datasets?name=" + setName + "&folderPath="+folderName);
        request.addHeader(authorisationHeader);
        request.bodyString(data ,ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8));
        HttpResponse response = request.execute().returnResponse();
        LOGGER.debug("Create request response: {}", response);
    }

    List<Column> readSourceSchema() throws IOException {
        Request request = Request.Get(url +"/api/datasets/"+ dataSetName + "/metadata");
        request.addHeader(authorisationHeader);

        DataPrepStreamMapper dataPrepStreamMapper = null;
        MetaData metaData;

        try {
            dataPrepStreamMapper = new DataPrepStreamMapper(request.execute().returnResponse().getEntity().getContent());
            metaData = dataPrepStreamMapper.getMetaData();
        } finally {
            if (dataPrepStreamMapper != null) {
                dataPrepStreamMapper.close();
            }
            logout();
        }

        return metaData.getColumns();
    }
}
