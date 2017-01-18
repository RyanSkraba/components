// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common.oauth.util;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Unit test for the org.talend.components.common.oauth.util.HttpsService class.
 *
 * @see HttpsService
 */
public class HttpsServiceTest {

    private HttpsService service;

    private String host = "127.0.0.1";

    private int port;

    @Before
    public void setUp() throws Exception {
        port = SocketUtils.findAvailableTcpPort(10000);
        service = new HttpsService(host, port, new HelloHandler());
    }

    @After
    public void tearDown() throws Exception {
        if (service != null) {
            service.stop();
        }
    }

    @Test
    public void httpsServiceShouldRespond() throws Exception {
        //@formatter:off
        given()
                .relaxedHTTPSValidation()
        .when()
                .get("https://" + host + ':' + port)
        .then()
                .assertThat()
                    .body(equalTo("<h1>Hello</h1>"))
                .assertThat()
                    .statusCode(200);
        //@formatter:on
    }


    private class HelloHandler extends AbstractHandler {

        /**
         * return "Hello"
         */
        @Override
        public void handle(String target, Request baseReq, HttpServletRequest req, HttpServletResponse resp)
                throws IOException, ServletException {
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print("<h1>Hello</h1>");
            resp.flushBuffer();
        }
    }
}
