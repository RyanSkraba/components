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

package org.talend.components.service.rest.impl;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.Validate.notNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.io.InputStream;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.ComponentService;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.service.rest.PropertiesController;
import org.talend.components.service.rest.dto.ConnectorDto;
import org.talend.components.service.rest.dto.PropertiesDto;
import org.talend.components.service.rest.dto.ValidationResultsDto;
import org.talend.components.service.rest.serialization.JsonSerializationHelper;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.serialize.jsonschema.PropertyTrigger;

@ServiceImplementation
public class PropertiesControllerImpl implements PropertiesController {

    private static final Logger log = getLogger(PropertiesControllerImpl.class);

    @Autowired
    private JsonSerializationHelper jsonSerializationHelper;

    @Autowired
    private DefinitionRegistryService definitionServiceDelegate;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private PropertiesHelpers propertiesHelpers;

    @Override
    public String getProperties(String definitionName, String formName) {
        notNull(definitionName, "Data store name cannot be null.");
        final Definition<?> definition = propertiesHelpers.getDefinition(definitionName);
        notNull(definition, "Could not find data store definition of name %s", definitionName);
        log.debug("Found data store definition {} for {}", definition, definitionName);
        return jsonSerializationHelper.toJson(
                definitionServiceDelegate.createProperties(definition, definitionName + " properties"), formName, definitionName);
    }

    @Override
    // TODO: Verify it is really what's wanted and not just the ValidationResult.Result.(OK|ERROR)
    public ResponseEntity<ValidationResultsDto> validateProperties(PropertiesDto propertiesContainer) {
        Properties properties = propertiesHelpers.propertiesFromDto(propertiesContainer);
        ValidationResult validationResult = properties.getValidationResult();
        // TODO: I really would prefer return 200 status code any time it process correctly and that the payload determine the
        // result of the analysis.
        // Here we use 400 return code for perfectly acceptable validation request but results with unaccepted properties.
        ResponseEntity<ValidationResultsDto> response;
        if (validationResult == null) {
            response = new ResponseEntity<>(new ValidationResultsDto(emptyList()), OK);
        } else {
            switch (validationResult.getStatus()) {
            case ERROR:
            case WARNING:
                response = new ResponseEntity<>(new ValidationResultsDto(validationResult), BAD_REQUEST);
                break;
            case OK:
            default:
                response = new ResponseEntity<>(new ValidationResultsDto(validationResult), OK);
            }
        }
        return response;
    }

    @Override
    public ResponseEntity<InputStreamResource> getIcon(String definitionName, DefinitionImageType imageType) {
        notNull(definitionName, "Definition name cannot be null.");
        notNull(imageType, "Definition image type cannot be null.");
        final Definition<?> definition = propertiesHelpers.getDefinition(definitionName);
        notNull(definition, "Could not find definition of name %s", definitionName);

        // Undefined and missing icon resources are simply 404.
        String imagePath = definition.getImagePath(imageType);
        if (imagePath == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        InputStream is = definition.getClass().getResourceAsStream(imagePath);
        if (is == null) {
            log.info("The image type %s should exist for %s at %s, but is missing. "
                    + "The component should provide this resource.", imageType, definitionName, imagePath);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // At this point, we have enough information for a correct response.
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();

        // Add the content type if it can be determined.
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String contentType = mimeTypesMap.getContentType(imagePath);
        if (contentType != null) {
            response = response.contentType(MediaType.parseMediaType(contentType));
        }

        return response.body(new InputStreamResource(is));
    }

    @Override
    public ResponseEntity<List<ConnectorDto>> getConnectors(String definitionName) {
        notNull(definitionName, "Definition name cannot be null.");
        final Definition<?> definition = propertiesHelpers.getDefinition(definitionName);
        notNull(definition, "Could not find definition of name %s", definitionName);

        // Error code 400 if the definition is not a component (Cannot have connectors).
        if (!(definition instanceof ComponentDefinition)) {
            throw TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).set("definitionClass",
                    definition.getClass().getName());
        }

        return new ResponseEntity<>(ConnectorDto.createConnectorList((ComponentDefinition) definition), OK);
    }

    @Override
    public ResponseEntity<String> triggerOnProperty(String definition, //
            PropertyTrigger trigger, //
            String property, //
            String formName, //
            PropertiesDto propertiesContainer) {
        Properties properties = propertiesHelpers.propertiesFromDto(propertiesContainer);

        String response;
        try {
            Properties updatedProperties;
            switch (trigger) {
            case VALIDATE:
                updatedProperties = componentService.validateProperty(property, properties);
                break;
            case BEFORE_ACTIVE:
                updatedProperties = componentService.beforePropertyActivate(property, properties);
                break;
            case BEFORE_PRESENT:
                updatedProperties = componentService.beforePropertyPresent(property, properties);
                break;
            case AFTER:
                updatedProperties = componentService.afterProperty(property, properties);
                break;
            default:
                throw new IllegalArgumentException("This enum does not contain this value: " + trigger);
            }
            response = jsonSerializationHelper.toJson(updatedProperties, formName, definition);
        } catch (IllegalStateException e) {
            log.info("Tried to execute an undefined trigger. It show either a bug in the calling client or the definition"
                    + " properties advertised a non-existent trigger", e);
            throw new UndefinedTriggerException(definition, property, trigger);
        } catch (Throwable throwable) {
            Exception exception = handleErrors(throwable);
            log.warn("Error validating property.", exception);
            // Letting common handler return a 500 error and correct message structure
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, exception);
        }
        return new ResponseEntity<>(response, OK);
    }

    @Override
    public String initializeProperties(String formName, PropertiesDto propertiesContainer) {
        Properties properties = propertiesHelpers.propertiesFromDto(propertiesContainer);
        if (properties == null) {
            return "{}";
        }
        properties.refreshLayout(properties.getPreferredForm(formName));
        return jsonSerializationHelper.toJson(formName, properties);
    }

    @Override
    public String getDatasetProperties(String definitionName, String formName, PropertiesDto propertiesContainer) {
        DatastoreDefinition<DatastoreProperties> datastoreDefinition = propertiesHelpers.getDataStoreDefinition(definitionName);
        notNull(datastoreDefinition, "Could not find data store definition of name %s", definitionName);
        DatastoreProperties properties = propertiesHelpers.propertiesFromDto(propertiesContainer);
        DatasetProperties datasetProperties = datastoreDefinition.createDatasetProperties(properties);
        return datasetProperties == null ? "{}" : jsonSerializationHelper.toJson(formName, datasetProperties);
    }

    private static Exception handleErrors(Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        return (Exception) throwable;
    }

}
