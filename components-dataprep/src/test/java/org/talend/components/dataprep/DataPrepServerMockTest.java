package org.talend.components.dataprep;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.test.SpringApp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
@WebIntegrationTest
public class DataPrepServerMockTest {

    @Test
    public void testsomething() throws IOException {
        Request request = Request.Get("http://localhost:8080");
        Response response = request.execute();
        System.out.println(response.returnContent().toString());
//        Assert.assertEquals(response.returnResponse().getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
    }

    @Test
    public void testLogin() throws IOException {
        Request request = Request.Post("http://localhost:8080/login?username=max&password=pass");
        Response response = request.execute();
//        response.returnContent();
        HttpResponse httpResponse = response.returnResponse();
        System.out.println(httpResponse.getStatusLine());
        System.out.println(Arrays.asList(httpResponse.getAllHeaders()).toString());
    }

    @Test
    public void testReadSchema() throws IOException {
        Request request = Request.
                Get("http://localhost:8080/api/datasets/db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e/metadata");
        Response response = request.execute();
        System.out.println(response.returnContent().asString());
    }

    @Test
    public void testCreateInLiveDataSetMode() throws IOException {
        URL connectionUrl = new URL("http://localhost:8080");
        HttpURLConnection urlConnection = (HttpURLConnection) connectionUrl.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "text/plain");
        urlConnection.setDoOutput(true);
        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write("Hello".getBytes());
        System.out.println(urlConnection.getResponseCode());
        urlConnection.disconnect();
    }
}
