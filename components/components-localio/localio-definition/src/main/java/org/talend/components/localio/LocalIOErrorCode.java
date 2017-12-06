// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.localio;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * Error codes for the {@link LocalIOComponentFamilyDefinition}.
 */
public enum LocalIOErrorCode implements ErrorCode {

    /** The schema value is not a correct JSON format. */
    CANNOT_PARSE_SCHEMA("CANNOT_PARSE_SCHEMA", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "schema"),

    /** The schema value is not a correct JSON format. */
    CANNOT_PARSE_JSON("CANNOT_PARSE_JSON", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "schema", "value"),

    /** The values are not correct Avro JSON.. */
    CANNOT_PARSE_AVRO_JSON("CANNOT_PARSE_AVRO_JSON", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "schema", "value"),

    /** JSON and CSV formats require at least one record in the value to infer the schema. */
    REQUIRE_AT_LEAST_ONE_RECORD("REQUIRE_AT_LEAST_ONE_RECORD", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    private final String code;

    private final int httpStatus;

    private final Collection<String> contextEntries;

    private LocalIOErrorCode(String code, int httpStatus, String... contextEntries) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.contextEntries = Arrays.asList(contextEntries);
    }

    @Override
    public String getProduct() {
        return "Talend";
    }

    @Override
    public String getGroup() {
        return LocalIOComponentFamilyDefinition.NAME;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public Collection<String> getExpectedContextEntries() {
        return contextEntries;
    }

    @Override
    public String getCode() {
        return code;
    }

    /**
     * Create an exception with the error code and context for {@link #CANNOT_PARSE_SCHEMA}.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @param schema The schema string the user was attempting to parse.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException createCannotParseSchema(Throwable cause, String schema) {
        return new TalendMsgRuntimeException(cause, CANNOT_PARSE_SCHEMA,
                ExceptionContext.withBuilder().put("schema", schema).build(), "The supplied Avro schema cannot be parsed.");
    }

    /**
     * Create an exception with the error code and context for {@link #CANNOT_PARSE_AVRO_JSON}.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @param schema The schema string the user was attempting to parse.
     * @param value The AVRO JSON string the user was attempting to parse.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException createCannotParseAvroJson(Throwable cause, String schema, String value) {
        return new TalendMsgRuntimeException(cause, CANNOT_PARSE_AVRO_JSON,
                ExceptionContext.withBuilder().put("schema", schema).put("value", value).build(),
                "The supplied Avro JSON value cannot be parsed.");
    }

    /**
     * Create an exception with the error code and context for {@link #CANNOT_PARSE_JSON}.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @param schema The schema string the user was attempting to parse.
     * @param value The JSON string the user was attempting to parse.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException createCannotParseJson(Exception cause, String schema, String value) {
        return new TalendMsgRuntimeException(cause, CANNOT_PARSE_JSON,
                ExceptionContext.withBuilder().put("schema", schema).put("value", value).build(),
                "The supplied value cannot be parsed.");
    }

    /**
     * Create an exception with the error code and context for {@link #REQUIRE_AT_LEAST_ONE_RECORD}.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException requireAtLeastOneRecord(Throwable cause) {
        return new TalendMsgRuntimeException(cause, REQUIRE_AT_LEAST_ONE_RECORD, ExceptionContext.withBuilder().build(),
                "At least one record is required in the value to proceed.");
    }

    /**
     * {@link TalendRuntimeException} with a reasonable user-friendly message in English.
     */
    private static class TalendMsgRuntimeException extends TalendRuntimeException {

        private final String localizedMessage;

        public TalendMsgRuntimeException(Throwable cause, ErrorCode code, ExceptionContext context, String localizedMessage) {
            super(code, cause, context);
            this.localizedMessage = localizedMessage;
        }

        @Override
        public String getMessage() {
            return getLocalizedMessage();
        }

        @Override
        public String getLocalizedMessage() {
            return localizedMessage;
        }
    }
}
