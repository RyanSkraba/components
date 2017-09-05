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
package org.talend.components.adapter.beam;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * Error codes for Beam.
 */
public enum BeamAdapterErrorCode implements ErrorCode {

    /**
     * The framework attempted to set a {@link org.talend.components.adapter.beam.coders.AvroSchemaHolder} supplier in a
     * {@link org.talend.components.adapter.beam.coders.LazyAvroCoder} when one was already set.
     */
    SCHEMA_REGISTRY_ALREADY_EXISTS("SCHEMA_REGISTRY_ALREADY_EXISTS", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "existing");

    private final String code;

    private final int httpStatus;

    private final Collection<String> contextEntries;

    private BeamAdapterErrorCode(String code, int httpStatus, String... contextEntries) {
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
        return "Beam";
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
     * Create an exception with the error code and context for {@link #SCHEMA_REGISTRY_ALREADY_EXISTS}. This should not
     * normally occur during normal construction of Beam Pipelines, but indicates a non-recoverable error when it does.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @param existing The current SchemaRegistrySupplier.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException createSchemaRegistryAlreadyExists(Throwable cause, String existing) {
        return new TalendMsgRuntimeException(cause, SCHEMA_REGISTRY_ALREADY_EXISTS,
                ExceptionContext.withBuilder().put("existing", existing).build(),
                "A schema registry supplier of type '" + existing + "' was already set.");
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
