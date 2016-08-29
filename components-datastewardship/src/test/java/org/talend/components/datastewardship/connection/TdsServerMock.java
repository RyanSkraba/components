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
package org.talend.components.datastewardship.connection;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TdsServerMock {

    public static final String AUTHORIZATION = "Basic b3duZXIxOm93bmVyMQ=="; // owner1/owner1 //$NON-NLS-1$

    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/api/v1/campaigns/owned/{campaignName}/tasks", method = RequestMethod.POST, consumes = {
            MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity createTasks(@RequestHeader(value = "Authorization") String authorization,
            @PathVariable String campaignName, @RequestBody Object request) throws Exception {
        if (!AUTHORIZATION.equals(authorization)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        checkNotNull(request);
        Map taskMap = (Map) ((List) request).get(0);
        JSONObject taskJson = new JSONObject(taskMap);

        if (taskJson.containsKey("record") && taskJson.containsKey("type") && taskJson.containsValue("RESOLUTION")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return new ResponseEntity(HttpStatus.OK);
        } else if (taskJson.containsValue("MERGING") && taskJson.containsKey("record") && taskJson.containsKey("sourceRecords")) {
            return new ResponseEntity(HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/api/v1/campaigns/owned/{name}", method = RequestMethod.GET, produces = { //$NON-NLS-1$
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getCampaign(@RequestHeader(value = "Authorization") String authorization,
            @PathVariable String name) throws Exception { // $NON-NLS-1$
        if (!AUTHORIZATION.equals(authorization)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (name.equals("perf-review-resolution")) { //$NON-NLS-1$
            InputStreamResource inputStream = new InputStreamResource(
                    TdsServerMock.class.getResourceAsStream("../campaign-resolution.json")); //$NON-NLS-1$
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        }
        if (name.equals("campaign1")) { //$NON-NLS-1$
            InputStreamResource inputStream = new InputStreamResource(
                    TdsServerMock.class.getResourceAsStream("../runtime/campaign-merging.json")); //$NON-NLS-1$
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        }
        return new ResponseEntity<InputStreamResource>(HttpStatus.BAD_REQUEST);
    }
    
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/api/v1/campaigns/owned/", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity createCampaign(@RequestHeader(value = "Authorization") String authorization, 
            @RequestBody Object request) throws Exception {
        if (!AUTHORIZATION.equals(authorization)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        checkNotNull(request);        
        
        String campaign = request.toString();
        if (campaign.contains("campaign") && campaign.contains("taskType") && campaign.contains("recordStructure")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
    
    @SuppressWarnings("rawtypes")    
    @RequestMapping(value = "/api/v1/campaigns/owned/{campaignName}/tasks/{state}", method = RequestMethod.GET, produces = { //$NON-NLS-1$
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getTasks(@RequestHeader(value = "Authorization") String authorization,  
            @PathVariable String campaignName, @PathVariable String state) throws Exception {
        if (!AUTHORIZATION.equals(authorization)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        if (campaignName.equals("perf-review-resolution") && state.equals("Performance_review_request")) { //$NON-NLS-1$ //$NON-NLS-2$
            InputStreamResource inputStream = new InputStreamResource(
                    TdsServerMock.class.getResourceAsStream("resolution-tasks.json")); //$NON-NLS-1$
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        } 
        if (campaignName.equals("campaign1") && state.equals("HR_review_requested")) { //$NON-NLS-1$ //$NON-NLS-2$
            InputStreamResource inputStream = new InputStreamResource(
                    TdsServerMock.class.getResourceAsStream("merging-tasks.json")); //$NON-NLS-1$
            return new ResponseEntity<InputStreamResource>(inputStream, HttpStatus.OK);
        }
        return new ResponseEntity<InputStreamResource>(HttpStatus.BAD_REQUEST);
    }

}
