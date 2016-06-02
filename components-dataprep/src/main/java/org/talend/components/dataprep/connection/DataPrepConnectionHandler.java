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
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DataPrepConnectionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepConnectionHandler.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(DataPrepConnectionHandler.class);
    public static final String API_DATASETS = "/api/datasets/";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TEXT_PLAIN = "text/plain";

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
            LOGGER.error(messages.getMessage("error.loginFailed", response.getStatusLine()));
            throw new IOException(messages.getMessage("error.loginFailed", response.getStatusLine()));
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
                LOGGER.error(messages.getMessage("error.logoutFailed", response.getStatusLine()));
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
            LOGGER.debug(messages.getMessage("error.validationFailed", e.getMessage()));
            throw new IOException(messages.getMessage("error.validationFailed", e));
        }
    }

    public DataPrepStreamMapper readDataSetIterator() throws IOException {
        Request request = Request.Get(url + API_DATASETS + dataSetName + "?metadata=false").addHeader(authorisationHeader);
        HttpResponse response = request.execute().returnResponse();
        if (returnStatusCode(response) != HttpServletResponse.SC_OK) {
            LOGGER.error(messages.getMessage("error.retrieveDatasetFailed", returnStatusCode(response)));
            throw new IOException(messages.getMessage("error.retrieveDatasetFailed", returnStatusCode(response)));
        }
        LOGGER.debug("Read DataSet Response: {} ", response);
        return new DataPrepStreamMapper(response.getEntity().getContent());
    }

    public OutputStream createInLiveDataSetMode() throws IOException {

        LOGGER.debug("DataSet name: " + dataSetName);

        // in live dataset, the request is a simple post to the livedataset url
        URL connectionUrl = new URL(url);
        urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty(CONTENT_TYPE, TEXT_PLAIN);
        urlConnection.setDoOutput(true);
        return urlConnection.getOutputStream();
    }

    public OutputStream update() throws IOException {
        URL connectionUrl = new URL(url + API_DATASETS + dataSetName);
        LOGGER.debug("Request is {}", connectionUrl);
        urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty(authorisationHeader.getName(), authorisationHeader.getValue());
        urlConnection.setRequestProperty(CONTENT_TYPE, TEXT_PLAIN);
        urlConnection.setDoOutput(true);
        return urlConnection.getOutputStream();
    }

    public OutputStream create() throws IOException {
        URL connectionUrl = new URL(url + "/api/datasets?name=" + dataSetName);
        urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty(authorisationHeader.getName(), authorisationHeader.getValue());
        urlConnection.setRequestProperty(CONTENT_TYPE, TEXT_PLAIN);
        urlConnection.setDoOutput(true);
        return urlConnection.getOutputStream();
    }

    public List<Column> readSourceSchema() throws IOException {
        Request request = Request.Get(url + API_DATASETS + dataSetName + "/metadata");
        request.addHeader(authorisationHeader);

        DataPrepStreamMapper dataPrepStreamMapper = null;
        MetaData metaData;

        try {
            HttpResponse response = request.execute().returnResponse();
            if (returnStatusCode(response) != HttpServletResponse.SC_OK) {
                LOGGER.error(messages.getMessage("error.retrieveSchemaFailed", returnStatusCode(response)));
                throw new IOException(messages.getMessage("error.retrieveSchemaFailed", returnStatusCode(response)));
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