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
import org.talend.components.dataprep.runtime.DataPrepOutputModes;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
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

    private DataPrepOutputModes mode;

    private HttpURLConnection urlConnection;

    private Header authorisationHeader;

    public DataPrepConnectionHandler(String url, String login, String pass, String dataSetName) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.dataSetName = dataSetName;
        LOGGER.debug("Url: {}", url);
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
                closeUrlConnection();
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

    private void closeUrlConnection() throws IOException {
        LOGGER.debug("urlConnection = " + urlConnection);
        int responseCode = urlConnection.getResponseCode();
        LOGGER.debug("Url connection response code: {}", responseCode);
        if (mode != null && responseCode != HttpServletResponse.SC_OK) {
            switch (mode) {
            case Create:
                urlConnection.disconnect();
                throw new IOException("Dataset exist on Dataprep server. Response code: " + responseCode);
            case Update:
                urlConnection.disconnect();
                throw new IOException("Wrong DatasetID. Response code: " + responseCode);
            case LiveDataset:
                urlConnection.disconnect();
                throw new IOException("Wrong url. Response code: " + responseCode);
            default:
                throw new UnsupportedOperationException();
            }

        }
        urlConnection.disconnect();
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

    public OutputStream write(DataPrepOutputModes mode) throws IOException {
        this.mode = mode;
        switch (mode) {
        case Create:
            return writeToServer("POST", requestEncoding());
        case Update:
            return writeToServer("PUT", url + API_DATASETS + dataSetName);
        case LiveDataset:
            return writeToServer("POST", url);
        default:
            throw new UnsupportedOperationException();
        }
    }

    private String requestEncoding() throws IOException {
        URI uri;
        try {
            URL localUrl = new URL(url);
            uri = new URI(localUrl.getProtocol(), null, localUrl.getHost(), localUrl.getPort(), "/api/datasets",
                    "name=" + dataSetName + "&tag=components", null);
            LOGGER.debug("Request is: {}", uri);
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.debug(messages.getMessage("error.wrongInputParameters", e));
            throw new IOException(messages.getMessage("error.wrongInputParameters", e));
        }
        return uri.toString();
    }

    private OutputStream writeToServer(String requestMethod, String request) throws IOException {
        URL connectionUrl = new URL(request);
        urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod(requestMethod);
        urlConnection.setRequestProperty(CONTENT_TYPE, TEXT_PLAIN);
        urlConnection.setDoOutput(true);
        urlConnection.connect();
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