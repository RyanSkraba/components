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
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;
@Ignore
public class TDataPrepConnectionHandlerTest {

    private static final String URL = "http://10.42.10.60:8888";
    private static final String LOGIN = "maksym@dataprep.com"; //"maksym@dataprep.com";
    private static final String PASS = "maksym"; //"maksym";

    @Test
    @Ignore
    public void validate() throws IOException {
        DataPrepConnectionHandler connectionHandler =
                new DataPrepConnectionHandler("http://10.42.10.60:8888","maksym@dataprep.com","maksym",
                "read", "sldfjsl");
        assertTrue(connectionHandler.validate());
    }

    @Test
    public void readSchema() throws IOException {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
                URL, LOGIN, PASS, "read", "0d3df0df4a4aca0529ef5755bd03519adb115248");
        System.out.println(connectionHandler.connect());
        for (Column column: connectionHandler.readSourceSchema()) {
            System.out.println(column);
        }
        connectionHandler.logout();
    }

    @Test
    public void readDataSchema() throws IOException {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
                URL, LOGIN, PASS, "read", "7291aa08-fed5-4267-83f5-ede7e7d66a72");
        System.out.println(connectionHandler.connect());
        for (Map<String,String> record: connectionHandler.readDataSet()) {
            for (Map.Entry<String,String> field: record.entrySet())
                System.out.println(field.getValue());
            System.out.println("\n");
        }
        connectionHandler.logout();
    }

    @Test
    public void logout() throws IOException {
        Request.Post(URL+"/logout").addHeader("Authorization","Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZW1vdGVTZXNzaW9uSWQiOiI5MzI0NjhiZS1mMWVhLTQ2YzctYTBhMC1jZTgyZWFhYWU4OWIiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiREFUQV9DVVJBVE9SIiwiREFUQV9TQ0lFTlRJU1QiXSwiaXNzIjoiZGF0YS1wcmVwIiwiZXhwIjoxNDYxODUwMzI2LCJpYXQiOjE0NjE4NDY3MjYsInVzZXJJZCI6InZpbmNlbnRAZGF0YXByZXAuY29tIiwianRpIjoiNThmODY1OWQtOWRjOC00YTUyLTk5ZmUtMTNiOTU0MTgzMjhhIn0.k14tGLc0mKPX73WAdfZSBQO8Ac47yRxF1HmQUMNS2XI").execute();
    }

    @Test
    public void schema() throws IOException {
        DataPrepConnectionHandler connectionHandler = new DataPrepConnectionHandler(
                URL, LOGIN, PASS, "read", "db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e");
        HttpResponse response = connectionHandler.connect();
        Header httpHead = response.getFirstHeader("Authorization");
        Request request = Request.Get(URL +"/api/datasets/"+ "db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e" + "/metadata");
        request.addHeader(httpHead);
        System.out.println(request.execute().returnContent().asString());
    }
}
