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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.dataprep.runtime.DataPrepOutputModes;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

public class DataPrepConnectionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepConnectionHandler.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(DataPrepConnectionHandler.class);

    public static final String API_DATASETS = "/api/datasets/";

    private final String url;

    private final String login;

    private final String pass;

    private final String dataSetName;

    private final String dataSetId;

    private DataPrepOutputModes mode;

    private HttpURLConnection urlConnection;

    private Header authorisationHeader;

    public DataPrepConnectionHandler(String url, String login, String pass, String dataSetId, String dataSetName) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.dataSetId = dataSetId;
        this.dataSetName = dataSetName;
        LOGGER.debug("Url: {}", url);
    }

    public HttpResponse connect() throws IOException {
        String encoding = "UTF-8";

        if ((url == null) || (login == null) || (pass == null)) {
            throw new IOException(messages.getMessage("error.loginFailed", "please set the url, email and password"));
        }

        Request request = Request.Post(url + "/login?username=" + URLEncoder.encode(login, encoding) + "&password="
                + URLEncoder.encode(pass, encoding) + "&client-app=studio");
        HttpResponse response = request.execute().returnResponse();
        authorisationHeader = response.getFirstHeader("Authorization");
        if (returnStatusCode(response) != HttpServletResponse.SC_OK && authorisationHeader == null) {
            String moreInformation = extractResponseInformationAndConsumeResponse(response);
            LOGGER.error(messages.getMessage("error.loginFailed", moreInformation));
            throw new IOException(messages.getMessage("error.loginFailed", moreInformation));
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
                LOGGER.error(messages.getMessage("error.logoutFailed", extractResponseInformationAndConsumeResponse(response)));
            }
        }
        return response;
    }

    private String fetchDataSetId() throws IOException {
        if (dataSetName == null || authorisationHeader == null) {
            return null;
        }

        String result = null;

        URI uri = null;
        URL localUrl = new URL(url);
        try {
            uri = new URI(localUrl.getProtocol(), null, localUrl.getHost(), localUrl.getPort(), "/api/datasets",
                    "name=" + dataSetName, null);
        } catch (URISyntaxException e) {
            throw new ComponentException(e);
        }

        Request request = Request.Get(uri);
        request.addHeader(authorisationHeader);
        HttpResponse response = request.execute().returnResponse();
        String content = extractResponseInformationAndConsumeResponse(response);

        if (returnStatusCode(response) != HttpServletResponse.SC_OK) {
            throw new IOException(content);
        }

        if (content != null && !"".equals(content)) {
            Object object = JsonReader.jsonToJava(content);
            if (object != null && object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (array.length > 0) {
                    @SuppressWarnings("rawtypes")
                    JsonObject jo = (JsonObject) array[0];
                    result = (String) jo.get("id");
                }
            }
        }

        return result;
    }

    private void closeUrlConnection() throws IOException {
        LOGGER.debug("urlConnection = " + urlConnection);
        int responseCode = urlConnection.getResponseCode();
        LOGGER.debug("Url connection response code: {}", responseCode);
        if (mode != null && responseCode != HttpServletResponse.SC_OK) {
            String errorMessage = extractResponseInformationAndConsumeResponse(urlConnection);

            // TODO adjust the exception information
            switch (mode) {
            case Create:
                urlConnection.disconnect();
                throw new IOException("Dataset exist on Dataprep server. Response information: " + errorMessage);
            case Update:
                urlConnection.disconnect();
                throw new IOException("Wrong DatasetID. Response information: " + errorMessage);
            case CreateOrUpdate:
                urlConnection.disconnect();
                throw new IOException("Response information: " + errorMessage);
            case LiveDataset:
                urlConnection.disconnect();
                throw new IOException("Wrong url. Response information: " + errorMessage);
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

        // apache hc-fluent cannot be used here are it does not support streaming
        final HttpGet httpGet = new HttpGet(url + API_DATASETS + dataSetId + "?fullContent=true");
        LOGGER.debug("getting dataset content from {}", httpGet.getURI());
        httpGet.addHeader(authorisationHeader);

        DefaultHttpClient client = new DefaultHttpClient();
        final HttpResponse response = client.execute(httpGet);
        LOGGER.debug("response is {}", response.getStatusLine());

        if (returnStatusCode(response) != HttpServletResponse.SC_OK) {
            String moreInformation = extractResponseInformationAndConsumeResponse(response);
            LOGGER.error(messages.getMessage("error.retrieveDatasetFailed", moreInformation));
            throw new IOException(messages.getMessage("error.retrieveDatasetFailed", moreInformation));
        }

        return new DataPrepStreamMapper(response.getEntity().getContent());
    }

    public OutputStream write(DataPrepOutputModes mode) throws IOException {
        this.mode = mode;
        switch (mode) {
        case Create:
            if (fetchDataSetId() != null) {
                throw new IOException("The dataset : " + dataSetName
                        + " exists in the server already, if you want to update it, please use the 'Update' or 'CreateOrUpdate' mode.");
            }
            return writeToServer("POST", requestEncoding());
        case Update:
            return writeToServer("PUT", url + API_DATASETS + dataSetId);
        case CreateOrUpdate:
            String dataSetId = fetchDataSetId();
            if (dataSetId != null) {
                return writeToServer("PUT", url + API_DATASETS + dataSetId);
            } else {
                return writeToServer("POST", requestEncoding());
            }
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

    private static final int CHUNK_SIZE = 4096;

    private OutputStream writeToServer(String requestMethod, String request) throws IOException {
        URL connectionUrl = new URL(request);
        urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod(requestMethod);
        if (authorisationHeader != null) {
            urlConnection.setRequestProperty(authorisationHeader.getName(), authorisationHeader.getValue());
        }
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        urlConnection.setRequestProperty("Accept", "application/json, text/plain");
        urlConnection.setChunkedStreamingMode(CHUNK_SIZE);
        urlConnection.setDoOutput(true);
        urlConnection.connect();
        return urlConnection.getOutputStream();
    }

    public List<Column> readSourceSchema() throws IOException {
        Request request = Request.Get(url + API_DATASETS + dataSetId + "/metadata");
        request.addHeader(authorisationHeader);

        DataPrepStreamMapper dataPrepStreamMapper = null;
        MetaData metaData;

        try {
            HttpResponse response = request.execute().returnResponse();
            if (returnStatusCode(response) != HttpServletResponse.SC_OK) {
                String moreInformation = extractResponseInformationAndConsumeResponse(response);
                LOGGER.error(messages.getMessage("error.retrieveSchemaFailed", moreInformation));
                throw new IOException(messages.getMessage("error.retrieveSchemaFailed", moreInformation));
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

    private String extractResponseInformationAndConsumeResponse(HttpResponse response) throws IllegalStateException, IOException {
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
            return extractResponseInformationAndConsumeResponse(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String extractResponseInformationAndConsumeResponse(HttpURLConnection connection) throws IOException {
        InputStream is = null;
        try {
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                is = connection.getErrorStream();
            }
            return extractResponseInformationAndConsumeResponse(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String extractResponseInformationAndConsumeResponse(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader sr = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = sr.readLine()) != null) {
            sb.append(line);
        }

        // TODO in fact, it's a json string, we should extract it
        return sb.toString();
    }
}