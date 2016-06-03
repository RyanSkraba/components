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

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.test.SpringApp;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
@WebIntegrationTest("server.port:0")
public class DataPrepConnectionHandlerTest {

    private DataPrepConnectionHandler connectionHandler;

    private static final String URL = "http://localhost:";

    private static final String LOGIN = "vincent@dataprep.com";

    private static final String PASS = "vincent";

    private static final String ID = "db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e";

    @Value("${local.server.port}")
    private int serverPort;

    @Before
    public void setConnectionHandler() {
        connectionHandler = new DataPrepConnectionHandler(URL+serverPort, LOGIN, PASS, ID);
    }

    @Test
    public void testLogin() throws IOException {
        HttpResponse response = connectionHandler.connect();
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        Assert.assertNotNull(response.getFirstHeader("Authorization"));
    }

    @Test(expected = IOException.class)
    public void testFailedLogin() throws IOException {
        connectionHandler = new DataPrepConnectionHandler(URL+serverPort, LOGIN, "wrong", "any");
        connectionHandler.connect();
    }

    @Test
    public void testLogout() throws IOException {
        connectionHandler.connect();
        HttpResponse response = connectionHandler.logout();
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testFailedLogout() throws IOException {
        connectionHandler = new DataPrepConnectionHandler(URL+serverPort, "testLogout", "testLogout", "any");
        Assert.assertEquals(200, returnStatusCode(connectionHandler.connect()));
        Assert.assertEquals(400, returnStatusCode(connectionHandler.logout()));
    }

    @Test
    public void testValidate() throws IOException {
        boolean isPassedWithoutException;
        try {
            connectionHandler.validate();
            isPassedWithoutException = true;
        } catch (IOException e) {
            isPassedWithoutException = false;
        }
        Assert.assertTrue("Validation method is passed without exceptions", isPassedWithoutException);
    }

    @Test(expected = IOException.class)
    public void testFailedValidate() throws IOException {
        connectionHandler = new DataPrepConnectionHandler(URL+serverPort, LOGIN, "wrong", "any");
        connectionHandler.validate();
    }

    @Test
    public void testReadSourceSchema() throws IOException {
        connectionHandler.connect();
        Assert.assertNotNull(connectionHandler.readSourceSchema());
    }

    @Test(expected = IOException.class)
    public void testFailedReadSourceSchema() throws IOException {
        connectionHandler = new DataPrepConnectionHandler(URL+serverPort, LOGIN, PASS, "any");
        connectionHandler.connect();
        connectionHandler.readSourceSchema();
    }

    @Test
    public void testCreateInLiveDataSetMode() throws IOException {
        Assert.assertEquals(200, returnStatusCode(connectionHandler.connect()));
        //Exception shouldn't be thrown.
        connectionHandler.createInLiveDataSetMode().write("Hello".getBytes());
        Assert.assertEquals(200, returnStatusCode(connectionHandler.logout()));
    }

    @Test
    public void testCreate() throws IOException {
        Assert.assertEquals(200, returnStatusCode(connectionHandler.connect()));
        //Exception shouldn't be thrown.
        connectionHandler.create().write("Hello".getBytes());
        Assert.assertEquals(200, returnStatusCode(connectionHandler.logout()));
    }

    @Test
    public void testNameWithSpacesInCreateMode() throws IOException {
        connectionHandler = new DataPrepConnectionHandler(URL + serverPort, LOGIN, PASS, "??Hello world");
        Assert.assertEquals(200, returnStatusCode(connectionHandler.connect()));
        //Exception shouldn't be thrown.
        connectionHandler.create().write("Hello".getBytes());
        Assert.assertEquals(200, returnStatusCode(connectionHandler.logout()));
    }

    @Test
    public void testUpdate() throws IOException {
        Assert.assertEquals(200, returnStatusCode(connectionHandler.connect()));
        //Exception shouldn't be thrown.
        connectionHandler.update().write("Hello".getBytes());
        Assert.assertEquals(200, returnStatusCode(connectionHandler.logout()));
    }

    @Test
    public void testReadDataSetIterator() throws IOException {
        Assert.assertEquals(200, returnStatusCode(connectionHandler.connect()));
        Assert.assertNotNull(connectionHandler.readDataSetIterator());
        Assert.assertEquals(200, returnStatusCode(connectionHandler.logout()));
    }

    @Test(expected = IOException.class)
    public void testFailedReadDataSetIterator() throws IOException {
        connectionHandler = new DataPrepConnectionHandler(URL+serverPort, LOGIN, PASS, "any");
        Assert.assertEquals(200, returnStatusCode(connectionHandler.connect()));
        connectionHandler.readDataSetIterator();
    }

    private int returnStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }
}
