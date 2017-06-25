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

import static org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_ARGUMENT;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.talend.components.service.rest.DefinitionType;
import org.talend.components.service.rest.DefinitionTypeConverter;
import org.talend.components.service.rest.dto.ConnectorTypology;
import org.talend.components.service.rest.dto.ConnectorTypologyConverter;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.serialize.jsonschema.PropertyTrigger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.CaseFormat;

@ControllerAdvice
public class ControllersConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ControllersConfiguration.class);

    @ExceptionHandler
    public ResponseEntity<ApiError> handleTalendExceptions(TalendRuntimeException e) {
        log.info("A Talend exception reached the API", e);
        Throwable cause = e.getCause();
        return buildUnexpectedErrorResponse(e.getCode(), cause == null ? e.getMessage() : cause.getMessage(), cause);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleMappingExceptions(MethodArgumentTypeMismatchException e) {
        log.info("A mapping exception reached the API", e);
        return buildUnexpectedErrorResponse(UNEXPECTED_ARGUMENT, e.getMessage(), e.getCause());
    }

    private static ResponseEntity<ApiError> buildUnexpectedErrorResponse(ErrorCode code, String message, Throwable cause) {
        ApiError dto = new ApiError();
        dto.setCode(code.getProduct() + "_" + code.getGroup() + "_" + code.getCode());
        dto.setMessage(message);
        dto.setMessageTitle("An unexpected error occurred");
        dto.setCause(cause == null ? null : cause.getMessage());
        return new ResponseEntity<>(dto, HttpStatus.valueOf(code.getHttpStatus()));
    }

    /**
     * Initialise Web binders to be able to use {@link PropertyTrigger} in camel case in {@link PathVariable}.
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PropertyTrigger.class, new PropertyEditorSupport() {

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                String upperUnderscoreCased = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_UNDERSCORE).convert(text);
                PropertyTrigger propertyTrigger = PropertyTrigger.valueOf(upperUnderscoreCased);
                setValue(propertyTrigger);
            }
        });
        binder.registerCustomEditor(DefinitionType.class, new DefinitionTypeConverter());
        binder.registerCustomEditor(ConnectorTypology.class, new ConnectorTypologyConverter());
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
                return PropertyTrigger
                        .valueOf(CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_UNDERSCORE).convert(jp.getValueAsString()));
            }
        }
    }

}
