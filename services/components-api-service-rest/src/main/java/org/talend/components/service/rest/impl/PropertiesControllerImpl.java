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

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.CaseFormat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
import org.talend.components.service.rest.serialization.JsonSerializationHelper;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.serialize.jsonschema.PropertyTrigger;

import static org.apache.commons.lang3.Validate.notNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.*;

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
        // Here we use 400 return code for perfectly acceptable validation request but results with unaccepted properties.
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
    public ResponseEntity<String> triggerOnProperty(@PathVariable("definition") String definition, //
                                                    @PathVariable("trigger") PropertyTrigger trigger, //
                                                    @PathVariable("property") String property, //
                                                    @RequestBody String formData) {
        final RuntimableDefinition<?, ?> runtimableDefinition = getDefinition(definition);
        notNull(definition, "Could not find data store definition of name %s", definition);
        Properties properties = getPropertiesFromJson(runtimableDefinition, formData);
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
            response = jsonSerializationHelper.toJson(updatedProperties, definition);
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
    public String getDatasetProperties(@PathVariable("definitionName") String definitionName,
                                       @RequestBody FormDataContainer formData) {
        DatastoreDefinition<DatastoreProperties> datastoreDefinition = definitionServiceDelegate.getDefinitionsMapByType(
                DatastoreDefinition.class).get(definitionName);
        notNull(datastoreDefinition, "Could not find data store definition of name %s", definitionName);
        DatastoreProperties properties = getPropertiesFromJson(datastoreDefinition, formData.getFormData());
        DatasetProperties datasetProperties = datastoreDefinition.createDatasetProperties(properties);
        return datasetProperties == null ? "{}" : jsonSerializationHelper.toJson(datasetProperties);
    }

    private static Exception handleErrors(Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        return (Exception) throwable;
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

    /**
     * Initialise Web binders to be able to use {@link PropertyTrigger} in camel case in {@link PathVariable}.
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PropertyTrigger.class, new PropertyEditorSupport() {

            public void setAsText(String text) throws IllegalArgumentException {
                String upperUnderscoreCased = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_UNDERSCORE).convert(text);
                PropertyTrigger propertyTrigger = PropertyTrigger.valueOf(upperUnderscoreCased);
                setValue(propertyTrigger);
            }
        });
    }

    /**
     * Configure Jackson serialization to parse and write {@link PropertyTrigger} in camel case.
     * <p>
     * NB: It can easily be modified to work on any kind of enum.
     */
    @JsonComponent
    public static class PropertyTriggerSerializer {

        public static class Serializer extends StdSerializer<PropertyTrigger> {

            public Serializer() {
                super(PropertyTrigger.class);
            }

            @Override
            public void serialize(PropertyTrigger value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeString(CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(value.name()));
            }
        }

        public static class Deserializer extends JsonDeserializer<PropertyTrigger> {

            @Override
            public PropertyTrigger deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                return PropertyTrigger.valueOf(
                        CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_UNDERSCORE).convert(jp.getValueAsString()));
            }
        }
    }

}
