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
package org.talend.components.dataprep.connection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DataPrepConnectionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepConnectionHandler.class);

    private final String url;

    private final String login;

    private final String pass;

    private final String dataSetName;

    private HttpURLConnection urlConnection;

    private Header authorisationHeader;

    public DataPrepConnectionHandler(String url, String login, String pass, String dataSetName) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.dataSetName = dataSetName;
    }

    public HttpResponse connect() throws IOException {
        Request request = Request.Post(url + "/login?username=" + login + "&password=" + pass + "&client-app=studio");
        HttpResponse response = request.execute().returnResponse();
        authorisationHeader = response.getFirstHeader("Authorization");
        if (returnStatusCode(response) != HttpServletResponse.SC_OK && authorisationHeader == null) {
            LOGGER.error("Failed to login to Dataprep server: {}", response.getStatusLine().toString());
            // TODO i18n
            throw new IOException("Failed to login to Dataprep server: " + response.getStatusLine().toString());
        }
        return response;
    }

    public HttpResponse logout() throws IOException {
        HttpResponse response;
        try {
            if (urlConnection != null) {
                int responseCode = urlConnection.getResponseCode();
                LOGGER.debug("Url connection response code: {}", responseCode);
                urlConnection.disconnect();
            }
        } finally {
            Request request = Request.Post(url + "/logout?client-app=STUDIO").addHeader(authorisationHeader);
            response = request.execute().returnResponse();
            if (returnStatusCode(response) != HttpServletResponse.SC_OK && authorisationHeader != null) {
                // TODO i18n
                LOGGER.error("Failed to logout to Dataprep server: {}", response.getStatusLine().toString());
            }
        }
        return response;
    }

    private int returnStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    public void validate() throws IOException {
        try {
            connect();
            logout();
        } catch (IOException e) {
            LOGGER.debug("Validation isn't passed. Reason: {}", e.getMessage());
            // TODO i18n
            throw new IOException("Validation isn't passed. Reason: " + e.getMessage(), e);
        }
    }

    public DataPrepStreamMapper readDataSetIterator() throws IOException {
        Request request = Request.Get(url + "/api/datasets/" + dataSetName + "?metadata=false").addHeader(authorisationHeader);
        HttpResponse current = request.execute().returnResponse();
        if (returnStatusCode(current) != HttpServletResponse.SC_OK) {
            LOGGER.error("Failed to retrieve DateSet from Dataprep server : " + returnStatusCode(current));
            // TODO i18n
            throw new IOException("Failed to connect to Dataprep server : " + returnStatusCode(current));
        }
        LOGGER.debug("Read DataSet Response: {} ", current);
        return new DataPrepStreamMapper(current.getEntity().getContent());
    }

    public OutputStream createInLiveDataSetMode() throws IOException {

        LOGGER.debug("DataSet name: " + dataSetName);

        // in live dataset, the request is a simple post to the livedataset url
        URL connectionUrl = new URL(url);
        urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        urlConnection.setDoOutput(true);
        return urlConnection.getOutputStream();
    }

    public OutputStream create() throws IOException {
        URL connectionUrl = new URL(url + "/api/datasets?name=" + dataSetName);
        urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty(authorisationHeader.getName(), authorisationHeader.getValue());
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        urlConnection.setDoOutput(true);
        return urlConnection.getOutputStream();
    }

    public List<Column> readSourceSchema() throws IOException {
        Request request = Request.Get(url + "/api/datasets/" + dataSetName + "/metadata");
        request.addHeader(authorisationHeader);

        DataPrepStreamMapper dataPrepStreamMapper = null;
        MetaData metaData;

        try {
            HttpResponse response = request.execute().returnResponse();
            if (returnStatusCode(response) != HttpServletResponse.SC_OK) {
                LOGGER.error("Failed to retrieve Schema from Dataprep server : " + returnStatusCode(response));
                // TODO i18n
                throw new IOException("Failed to connect to Dataprep server : " + returnStatusCode(response));
            }
            dataPrepStreamMapper = new DataPrepStreamMapper(response.getEntity().getContent());
            metaData = dataPrepStreamMapper.getMetaData();
        } finally {
            if (dataPrepStreamMapper != null) {
                dataPrepStreamMapper.close();
            }
        }

        return metaData.getColumns();
    }
}