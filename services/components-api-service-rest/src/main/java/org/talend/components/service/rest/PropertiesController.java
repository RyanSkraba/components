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
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.talend.components.service.rest.dto.ConnectorDto;
import org.talend.components.service.rest.dto.SerPropertiesDto;
import org.talend.components.service.rest.dto.UiSpecsPropertiesDto;
import org.talend.components.service.rest.dto.ValidationResultsDto;
import org.talend.daikon.annotation.ApiVersion;
import org.talend.daikon.annotation.Service;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.serialize.jsonschema.PropertyTrigger;

@Service(name = "PropertiesController")
@RequestMapping("properties")
@ApiVersion(ServiceConstants.V0)
public interface PropertiesController {

    /**
     * The registered media type for SVG. Not available in {@link MediaType}.
     */
    String IMAGE_SVG_VALUE = "image/svg+xml";

    @RequestMapping(value = "{definitionName}", method = GET, produces = ServiceConstants.UI_SPEC_CONTENT_TYPE)
    String getProperties(@PathVariable("definitionName") String definitionName,
            @RequestParam(required = false, defaultValue = Form.MAIN) String formName);

    /**
     * Get a properties (UI spec) with a JSON IO payload that contains existing data.
     */
    @RequestMapping(value = "uispec", method = POST, consumes = ServiceConstants.JSONIO_CONTENT_TYPE, produces = ServiceConstants.UI_SPEC_CONTENT_TYPE)
    String initializeProperties(@RequestParam(required = false, defaultValue = Form.MAIN) String formName,
            @RequestBody SerPropertiesDto propertiesContainer);

    /**
     * Get a properties (UI spec) with a ui-properties payload that contains existing data.
     */
    @RequestMapping(value = "uispec", method = POST, consumes = { ServiceConstants.UI_SPEC_CONTENT_TYPE,
            APPLICATION_JSON_UTF8_VALUE }, produces = ServiceConstants.UI_SPEC_CONTENT_TYPE)
    String initializeProperties(@RequestParam(required = false, defaultValue = Form.MAIN) String formName,
            @RequestBody UiSpecsPropertiesDto propertiesContainer);

    /** Validate the coherence of a set of properties for a specific component. **/
    @RequestMapping(value = "validate", method = POST, consumes = { ServiceConstants.UI_SPEC_CONTENT_TYPE,
            APPLICATION_JSON_UTF8_VALUE })
    ResponseEntity<ValidationResultsDto> validateProperties(@RequestBody UiSpecsPropertiesDto propertiesContainer);

    /** Validate the coherence of a set of properties for a specific component. **/
    @RequestMapping(value = "validate", method = POST, consumes = ServiceConstants.JSONIO_CONTENT_TYPE)
    ResponseEntity<ValidationResultsDto> validateProperties(@RequestBody SerPropertiesDto propertiesContainer);

    /**
     * Gets an image resource for the properties described by the given definition.
     *
     * @param definitionName The definition to get resources from.
     * @param imageType The type of image fetched.
     * @return The image as a resource or 404 if the definitionName exists, but does not provide this resource.
     */
    @RequestMapping(value = "{definitionName}/icon/{imageType}", method = GET, produces = { IMAGE_PNG_VALUE, IMAGE_SVG_VALUE })
    ResponseEntity<InputStreamResource> getIcon(@PathVariable("definitionName") String definitionName,
            @PathVariable("imageType") DefinitionImageType imageType);

    /**
     * Gets connector information for the properties and a given definition.
     *
     * @param definitionName The definition to use when getting connectors for some properties..
     * @return
     */
    @RequestMapping(value = "{definitionName}/connectors", method = GET, produces = ServiceConstants.UI_SPEC_CONTENT_TYPE)
    ResponseEntity<List<ConnectorDto>> getConnectors(@PathVariable("definitionName") String definitionName);

    /** execute a trigger on one field. */
    @RequestMapping(value = "trigger/{trigger}/{property}", method = POST, consumes = {
            ServiceConstants.UI_SPEC_CONTENT_TYPE,
            APPLICATION_JSON_UTF8_VALUE }, produces = ServiceConstants.UI_SPEC_CONTENT_TYPE)
    ResponseEntity<String> triggerOnProperty(@PathVariable("trigger") PropertyTrigger trigger, //
            @PathVariable("property") String property, //
            @RequestParam(required = false, defaultValue = Form.MAIN) String formName, //
            @RequestBody UiSpecsPropertiesDto propertiesContainer);

    /** Get dataset properties from ui-properties of an existing datastore **/
    @RequestMapping(value = "dataset", method = POST, consumes = { ServiceConstants.UI_SPEC_CONTENT_TYPE,
            APPLICATION_JSON_UTF8_VALUE }, produces = ServiceConstants.UI_SPEC_CONTENT_TYPE)
    String getDatasetProperties(@RequestParam(required = false, defaultValue = Form.MAIN) String formName, //
            @RequestBody UiSpecsPropertiesDto propertiesContainer);

    /** Get dataset properties from jsonio serialized existing datastore **/
    @RequestMapping(value = "dataset", method = POST, consumes = ServiceConstants.JSONIO_CONTENT_TYPE, produces = ServiceConstants.UI_SPEC_CONTENT_TYPE)
    String getDatasetProperties(@RequestParam(required = false, defaultValue = Form.MAIN) String formName, //
            @RequestBody SerPropertiesDto propertiesContainer);

    /** Serialize the ui-specs into a json-io persitable representation of properties **/
    @RequestMapping(value = "serialize", method = POST, consumes = ServiceConstants.UI_SPEC_CONTENT_TYPE, produces = ServiceConstants.JSONIO_CONTENT_TYPE) SerPropertiesDto serialize(
            @RequestBody UiSpecsPropertiesDto propertiesContainer);

}
