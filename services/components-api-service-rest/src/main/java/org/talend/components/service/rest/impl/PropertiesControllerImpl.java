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

package org.talend.components.service.rest.impl;

import static org.apache.commons.lang3.Validate.*;
import static org.slf4j.LoggerFactory.*;
import static org.springframework.http.HttpStatus.*;
import static org.talend.daikon.properties.ValidationResult.Result.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.talend.components.api.RuntimableDefinition;
import org.talend.components.api.service.ComponentService;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.service.rest.FormDataContainer;
import org.talend.components.service.rest.PropertiesController;
import org.talend.components.service.rest.dto.PropertiesValidationResponse;
import org.talend.components.service.rest.dto.PropertyValidationResponse;
import org.talend.components.service.rest.serialization.JsonSerializationHelper;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

@ServiceImplementation
public class PropertiesControllerImpl implements PropertiesController {

    private static final Logger log = getLogger(PropertiesControllerImpl.class);

    @Autowired
    private JsonSerializationHelper jsonSerializationHelper;

    @Autowired
    private DefinitionRegistryService definitionServiceDelegate;

    @Autowired
    private ComponentService componentService;

    @Override
    public String getProperties(@PathVariable("name") String definitionName) {
        notNull(definitionName, "Data store name cannot be null.");
        final RuntimableDefinition<?, ?> definition = getDefinition(definitionName);
        notNull(definition, "Could not find data store definition of name %s", definitionName);
        log.debug("Found data store definition {} for {}", definition, definitionName);
        return jsonSerializationHelper.toJson(definitionServiceDelegate.createProperties(definition, ""), definitionName);
    }

    @Override
    public ResponseEntity<PropertiesValidationResponse> validateProperties(@PathVariable("definitionName") String definitionName,
                                                                           @RequestBody FormDataContainer formData) {
        notNull(definitionName, "Data store name cannot be null.");
        final RuntimableDefinition<?, ?> definition = getDefinition(definitionName);
        notNull(definition, "Could not find data store definition of name %s", definitionName);
        Properties properties = getPropertiesFromJson(definition, formData.getFormData());
        ValidationResult validationResult = properties.getValidationResult();
        // TODO: I really would prefer return 200 status code any time it process correctly and that the payload determine the
        // result of the analysis.
        // Here we use 400 return code for perfectly acceptable validation request but with result with unaccepted properties.
        ResponseEntity<PropertiesValidationResponse> response;
        if (validationResult == null) {
            // Workaround for null validation results that should not happen
            response = new ResponseEntity<>(NO_CONTENT);
        } else {
            switch (validationResult.getStatus()) {
            case ERROR:
            case WARNING:
                response = new ResponseEntity<>(new PropertiesValidationResponse(validationResult), BAD_REQUEST);
                break;
            case OK:
            default:
                response = new ResponseEntity<>(NO_CONTENT);
            }
        }
        return response;
    }

    @Override
    public ResponseEntity<PropertyValidationResponse> validateProperty(@PathVariable("definitionName") String definitionName,
            @RequestBody FormDataContainer formData, @PathVariable("propName") String propName) {
        // TODO: this is not conform to the SPEC => to rewrite
        final RuntimableDefinition<?, ?> definition = getDefinition(definitionName);
        notNull(definition, "Could not find data store definition of name %s", definitionName);
        Properties properties = getPropertiesFromJson(definition, formData.getFormData());
        ResponseEntity<PropertyValidationResponse> response;
        try {
            componentService.validateProperty(propName, properties);
            ValidationResult validationResult = properties.getValidationResult();

            if (validationResult == null) {
                // Workaround for null validation results that should not happen
                response = new ResponseEntity<>(NO_CONTENT);
            } else {
                switch (validationResult.getStatus()) {
                case ERROR:
                case WARNING:
                    response = new ResponseEntity<>(new PropertyValidationResponse(validationResult.getStatus()), BAD_REQUEST);
                    break;
                case OK:
                default:
                    response = new ResponseEntity<>(NO_CONTENT);
                }
            }
        } catch (Throwable throwable) {
            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            log.warn("Error validating property.");
            response = new ResponseEntity<>(new PropertyValidationResponse(ERROR), BAD_REQUEST);
        }
        return response;
    }

    @Override
    public String getDatasetProperties(@PathVariable("definitionName") String definitionName,
                                       @RequestBody FormDataContainer formData) {
        DatastoreDefinition<DatastoreProperties> datastoreDefinition = definitionServiceDelegate
                .getDefinitionsMapByType(DatastoreDefinition.class).get(definitionName);
        notNull(datastoreDefinition, "Could not find data store definition of name %s", definitionName);
        DatastoreProperties properties = getPropertiesFromJson(datastoreDefinition, formData.getFormData());
        DatasetProperties datasetProperties = datastoreDefinition.createDatasetProperties(properties);
        return datasetProperties == null ? "{}" : jsonSerializationHelper.toJson(datasetProperties);
    }

    private <T extends Properties, U> T getPropertiesFromJson(RuntimableDefinition<T, U> datastoreDefinition,
                                                              String formDataJson) {
        T properties = definitionServiceDelegate.createProperties(datastoreDefinition, "");
        return jsonSerializationHelper.toProperties(new ByteArrayInputStream(formDataJson.getBytes(StandardCharsets.UTF_8)),
                properties);
    }

    private RuntimableDefinition<?, ?> getDefinition(String definitionName) {
        return definitionServiceDelegate.getDefinitionsMapByType(RuntimableDefinition.class).get(definitionName);
    }
}
