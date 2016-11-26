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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DataPrepServerMock {

    private static final String TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZW1vdGVTZXNzaW9uSWQiOiI5MzI0NjhiZS1mMWVhLTQ2YzctYTBhMC1jZTgyZWFhYWU4OWIiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiREFUQV9DVVJBVE9SIiwiREFUQV9TQ0lFTlRJU1QiXSwiaXNzIjoiZGF0YS1wcmVwIiwiZXhwIjoxNDYxODUwMzI2LCJpYXQiOjE0NjE4NDY3MjYsInVzZXJJZCI6InZpbmNlbnRAZGF0YXByZXAuY29tIiwianRpIjoiNThmODY1OWQtOWRjOC00YTUyLTk5ZmUtMTNiOTU0MTgzMjhhIn0.k14tGLc0mKPX73WAdfZSBQO8Ac47yRxF1HmQUMNS2XI";

    private static final String ACCEPT = "application/json, text/plain";

    HttpHeaders headers;

    private String lastTag;

    private String lastName;

    private String lastReceivedLiveDataSetContent;

    public void clear() {
        lastReceivedLiveDataSetContent = null;
        lastTag = null;
        lastName = null;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity login(@RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password) {
        headers = new HttpHeaders();
        if (username.equals("vincent@dataprep.com") && (password.equals("vincent"))) {
            headers.add("Authorization", TOKEN);
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        if (username.equals("testLogout") && password.equals("testLogout")) {
            headers.add("Authorization", "testLogout");
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        if (username.equals("username+") && (password.equals("+password"))) {
            headers.add("Authorization", TOKEN);
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity logout(@RequestHeader(value = "Authorization") String authorization) throws IOException {
        if (authorization.equals(TOKEN)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/api/datasets/{id}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InputStreamResource> readDataSet(@PathVariable String id,
            @RequestParam(value = "fullContent") boolean fullContent) {
        if (id.equals("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e") && fullContent) {
            InputStreamResource inputStream = new InputStreamResource(
                    DataPrepServerMock.class.getResourceAsStream("dataset.json"));
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        }
        return new ResponseEntity<InputStreamResource>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/api/datasets/{id}/metadata", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> readSourceSchema(@PathVariable String id) {
        if (id.equals("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e")) {
            InputStreamResource inputStream = new InputStreamResource(
                    DataPrepServerMock.class.getResourceAsStream("metadata.json"));
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        }
        return new ResponseEntity<InputStreamResource>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/api/datasets", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getDataSetIdByName(@RequestParam(value = "name") String name) {
        if ("my_existed_dataset".equals(name)) {
            InputStreamResource inputStream = new InputStreamResource(
                    DataPrepServerMock.class.getResourceAsStream("success_fetch_dataset_id_by_name_response.json"));
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        } else {
            InputStreamResource inputStream = new InputStreamResource(
                    DataPrepServerMock.class.getResourceAsStream("fail_fetch_dataset_id_by_name_response.json"));
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/datasets", method = RequestMethod.POST)
    public ResponseEntity create(@RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "Accept") String accept, @RequestParam(value = "name") String name,
            @RequestParam(value = "tag") String tag, InputStream inputStream) throws IOException {
        if (!TOKEN.equals(authorization)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (!ACCEPT.equals(accept)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }

        checkNotNull(inputStream);
        lastTag = tag;
        lastName = name;
        if (name.equals("mydataset") || name.equals("??Hello world")) {
            lastReceivedLiveDataSetContent = IOUtils.toString(inputStream);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/api/datasets/{id}", method = RequestMethod.PUT)
    public ResponseEntity update(@RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "Accept") String accept, @PathVariable String id, InputStream inputStream) throws IOException {
        if (!TOKEN.equals(authorization)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (!ACCEPT.equals(accept)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }

        checkNotNull(inputStream);
        if (id.equals("db119c7d-33fd-46f5-9bdc-1e8cf54d4d1e")) {
            lastReceivedLiveDataSetContent = IOUtils.toString(inputStream);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity createInLiveDataSet(@RequestHeader(value = "Accept") String accept, InputStream inputStream)
            throws IOException {
        if (!ACCEPT.equals(accept)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }

        checkNotNull(inputStream);
        lastReceivedLiveDataSetContent = IOUtils.toString(inputStream);
        return new ResponseEntity(HttpStatus.OK);
    }

    public String getLastReceivedLiveDataSetContent() {
        return lastReceivedLiveDataSetContent;
    }

    String getLastTag() {
        return lastTag;
    }

    String getLastName() {
        return lastName;
    }
}
