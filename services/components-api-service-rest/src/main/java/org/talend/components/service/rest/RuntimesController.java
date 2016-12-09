//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.components.service.rest.dto.DatasetConnectionInfo;
import org.talend.components.service.rest.dto.ValidationResultsDto;
import org.talend.daikon.annotation.Service;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Service(name = "RuntimesController")
@RequestMapping(value = "runtimes")
public interface RuntimesController {

    /**
     * Avro mime types as specified at <a href="http://avro.apache.org/docs/current/spec.html#HTTP+as+Transport>http://avro.apache.org/docs/current/spec.html#HTTP+as+Transport</a>
     * and discussed at <a href="https://issues.apache.org/jira/browse/AVRO-488>https://issues.apache.org/jira/browse/AVRO-488</a>
     */
    String AVRO_BINARY_MIME_TYPE_OFFICIAL_INVALID = "avro/binary";
    String AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID = "application/x-avro-binary";
    String AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED = "application/avro-binary";

    String AVRO_JSON_MIME_TYPE_OFFICIAL_INVALID = "avro/json";
    String AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID = "application/x-avro-json";
    String AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED = "application/avro-json";

    /** Validate connection to datastore */
    @RequestMapping(value = "{dataStoreDefinitionName}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    ResponseEntity<ValidationResultsDto> validateDataStoreConnection(
            @PathVariable("dataStoreDefinitionName") String dataStoreDefinitionName, InputStream formData);

    /** Validate connection to datastore. Should it be GET method? */
    @RequestMapping(value = "{datasetDefinitionName}/schema", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    String getDatasetSchema(@PathVariable("datasetDefinitionName") String datasetDefinitionName,
                            @RequestBody DatasetConnectionInfo connectionInfo) throws IOException;

    /** Get dataset content. Should it be GET method? */
    @RequestMapping(value = "{datasetDefinitionName}/data", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = {
            APPLICATION_JSON_UTF8_VALUE, AVRO_JSON_MIME_TYPE_OFFICIAL_INVALID, AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID,
            AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED })
    StreamingResponseBody getDatasetData(@PathVariable("datasetDefinitionName") String datasetDefinitionName,
                                         @RequestBody DatasetConnectionInfo connectionInfo,
                                         @RequestParam(value = "from", required = false) Integer from,
                                         @RequestParam(value = "limit", required = false) Integer limit);

    @RequestMapping(value = "{datasetDefinitionName}/data", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = {
            AVRO_BINARY_MIME_TYPE_OFFICIAL_INVALID, AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID,
            AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED })
    StreamingResponseBody getDatasetDataAsBinary(@PathVariable("datasetDefinitionName") String datasetDefinitionName,
                                                 @RequestBody DatasetConnectionInfo connectionInfo,
                                                 @RequestParam(value = "from", required = false) Integer from,
                                                 @RequestParam(value = "limit", required = false) Integer limit);

}
