// ==============================================================================
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
// ==============================================================================

package org.talend.components.service.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.talend.components.service.rest.dto.SerPropertiesDto;
import org.talend.components.service.rest.dto.UiSpecsPropertiesDto;
import org.talend.components.service.rest.dto.ValidationResultsDto;
import org.talend.daikon.annotation.ApiVersion;
import org.talend.daikon.annotation.Service;

@Service(name = "RuntimesController")
@RequestMapping("runtimes")
@ApiVersion(ServiceConstants.V0)
public interface RuntimesController {

    /**
     * Avro mime types as specified at <a
     * href="http://avro.apache.org/docs/current/spec.html#HTTP+as+Transport>http://avro.apache.org/docs/current/spec.html#HTTP+as+Transport</a>
     * and discussed at <a
     * href="https://issues.apache.org/jira/browse/AVRO-488>https://issues.apache.org/jira/browse/AVRO-488</a>
     */
    String AVRO_BINARY_MIME_TYPE_OFFICIAL_INVALID = "avro/binary";

    String AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID = "application/x-avro-binary";

    String AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED = "application/avro-binary";

    String AVRO_JSON_MIME_TYPE_OFFICIAL_INVALID = "avro/json";

    String AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID = "application/x-avro-json";

    String AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED = "application/avro-json";

    /** Validate connection to datastore from ui-specs */
    @RequestMapping(value = "check", method = POST, consumes = { APPLICATION_JSON_UTF8_VALUE,
            ServiceConstants.UI_SPEC_CONTENT_TYPE })
    ResponseEntity<ValidationResultsDto> validateDataStoreConnection(@RequestBody UiSpecsPropertiesDto propertiesContainer);

    /** Validate connection to datastore from jsonio */
    @RequestMapping(value = "check", method = POST, consumes = ServiceConstants.JSONIO_CONTENT_TYPE)
    ResponseEntity<ValidationResultsDto> validateDataStoreConnection(@RequestBody SerPropertiesDto propertiesContainer);

    /** return the schema associated to a given dataset from ui-specs. */
    @RequestMapping(value = "schema", method = POST, consumes = { APPLICATION_JSON_UTF8_VALUE,
            ServiceConstants.UI_SPEC_CONTENT_TYPE }, produces = APPLICATION_JSON_UTF8_VALUE)
    String getDatasetSchema(@RequestBody UiSpecsPropertiesDto connectionInfo) throws IOException;

    /** return the schema associated to a given dataset from json-io. */
    @RequestMapping(value = "schema", method = POST, consumes = ServiceConstants.JSONIO_CONTENT_TYPE, produces = APPLICATION_JSON_UTF8_VALUE)
    String getDatasetSchema(@RequestBody SerPropertiesDto connectionInfo) throws IOException;

    /** Get dataset content from ui-specs */
    @RequestMapping(value = "data", method = POST, consumes = { APPLICATION_JSON_UTF8_VALUE,
            ServiceConstants.UI_SPEC_CONTENT_TYPE }, produces = { APPLICATION_JSON_UTF8_VALUE,
                    AVRO_JSON_MIME_TYPE_OFFICIAL_INVALID, AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID,
                    AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED })
    Void getDatasetData(@RequestBody UiSpecsPropertiesDto connectionInfo,
            @RequestParam(value = "from", required = false) Integer from,
            @RequestParam(value = "limit", required = false) Integer limit, OutputStream response);

    /** Get dataset content from json-io */
    @RequestMapping(value = "data", method = POST, consumes = ServiceConstants.JSONIO_CONTENT_TYPE, produces = {
            APPLICATION_JSON_UTF8_VALUE, AVRO_JSON_MIME_TYPE_OFFICIAL_INVALID, AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID,
            AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED })
    Void getDatasetData(@RequestBody SerPropertiesDto connectionInfo,
            @RequestParam(value = "from", required = false) Integer from,
            @RequestParam(value = "limit", required = false) Integer limit, OutputStream response);

    /** get dataset content in binary form from ui-specs */
    @RequestMapping(value = "data", method = POST, consumes = { APPLICATION_JSON_UTF8_VALUE,
            ServiceConstants.UI_SPEC_CONTENT_TYPE }, produces = { AVRO_BINARY_MIME_TYPE_OFFICIAL_INVALID,
                    AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID, AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED })
    Void getDatasetDataAsBinary(@RequestBody UiSpecsPropertiesDto connectionInfo,
            @RequestParam(value = "from", required = false) Integer from,
            @RequestParam(value = "limit", required = false) Integer limit, OutputStream response);

    /** get dataset content in binary form from json-io */
    @RequestMapping(value = "data", method = POST, consumes = ServiceConstants.JSONIO_CONTENT_TYPE, produces = {
            AVRO_BINARY_MIME_TYPE_OFFICIAL_INVALID, AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID,
            AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED })
    Void getDatasetDataAsBinary(@RequestBody SerPropertiesDto connectionInfo,
            @RequestParam(value = "from", required = false) Integer from,
            @RequestParam(value = "limit", required = false) Integer limit, OutputStream response);

    /**
     * Write Data using a Talend component.
     * <p>
     * The payload sent to this endpoint must contain all metadata then data to write data. Payload is formatted using
     * {@link org.talend.components.service.rest.impl.DatasetWritePayload} that contains first components properties to
     * open the right output then a streaming array of data in AVRO format.
     * </p>
     */
    @RequestMapping(value = "data", method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    void writeData(InputStream payload) throws IOException;

}
