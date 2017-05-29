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
package org.talend.components.simplefileio;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * Error codes for the {@link SimpleFileIOComponentFamilyDefinition}.
 */
public enum SimpleFileIOErrorCode implements ErrorCode {

    /** The user is attempting to create an output on a path that already exists, but is not set to overwrite. */
    OUTPUT_ALREADY_EXISTS("OUTPUT_ALREADY_EXISTS", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "path"),

    /** The user is attempting to create an output on a path that they do not have permission to access. */
    OUTPUT_NOT_AUTHORIZED("OUTPUT_NOT_AUTHORIZED", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "path", "username"),

    /** The user is attempting to read from a path that they do not have permission to access. */
    INPUT_NOT_AUTHORIZED("INPUT_NOT_AUTHORIZED", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "path", "username");

    private final String code;

    private final int httpStatus;

    private final Collection<String> contextEntries;

    private SimpleFileIOErrorCode(String code, int httpStatus, String... contextEntries) {
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
        return SimpleFileIOComponentFamilyDefinition.NAME;
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
     * Create an exception with the error code and context for {@link #OUTPUT_ALREADY_EXISTS}.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @param path The path that the user was attempting to write to.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException createOutputAlreadyExistsException(Throwable cause, String path) {
        return new TalendMsgRuntimeException(cause, OUTPUT_ALREADY_EXISTS, ExceptionContext.withBuilder().put("path", path)
                .build(), "The path " + path + " already exists. Please remove it manually.");
        // TODO: when overwrite is enabled, add "or enable the overwrite property."
    }

    /**
     * Create an exception with the error code and context for {@link #OUTPUT_NOT_AUTHORIZED}.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @param username The username trying to write to the path.
     * @param path The path that the user was attempting to write to.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException createOutputNotAuthorized(Throwable cause, String username, String path) {
        if (username == null)
            return new TalendMsgRuntimeException(cause, OUTPUT_NOT_AUTHORIZED, ExceptionContext.withBuilder().put("path", path)
                    .put("username", username).build(), "Can not write to " + path
                    + ". Please check user permissions or existence of base directory.");
        else
            return new TalendMsgRuntimeException(cause, OUTPUT_NOT_AUTHORIZED, ExceptionContext.withBuilder().put("path", path)
                    .put("username", username).build(), username + " can not write to " + path
                    + ". Please check user permissions or existence of base directory.");
    }

    /**
     * Create an exception with the error code and context for {@link #INPUT_NOT_AUTHORIZED}.
     *
     * @param cause The technical exception that was caught when the error occurred.
     * @param username The username trying to write to the path.
     * @param path The path that the user was attempting to read from.
     * @return An exception corresponding to the error code.
     */
    public static TalendRuntimeException createInputNotAuthorized(Throwable cause, final String username, final String path) {
        final String localizedMessage;
        if (username == null)
            localizedMessage = "Can not read from " + path + ". Please check user permissions or existence of base directory.";
        else
            localizedMessage = username + " can not read from " + path
                    + ". Please check user permissions or existence of base directory.";
        return new TalendMsgRuntimeException(cause, INPUT_NOT_AUTHORIZED, ExceptionContext.withBuilder().put("path", path)
                .put("username", username).build(), localizedMessage);
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
