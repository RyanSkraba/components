// ============================================================================
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
// ============================================================================
package org.talend.components.api.exception;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.properties.ValidationResult;

public class ComponentException extends TalendRuntimeException {

    private static final long serialVersionUID = -84662653622272070L;

    private ValidationResult validationResult;

    public ComponentException(ErrorCode code) {
        super(code);
    }

    public ComponentException(Throwable cause) {
        super(CommonErrorCodes.UNEXPECTED_EXCEPTION, cause);
    }

    /**
     * This create a {@link CommonErrorCodes#UNEXPECTED_EXCEPTION} error with a special context with a "message" key and
     * the <code>vr.getMessage()</code> value, and the "status" key with the value <code>vr.getStatus()</code>. <br>
     * This is deprecated cause an exception should not be made of a {@link ValidationResult}. A
     * {@link ValidationResult} represents the current status of an object and there should be no reason to use it for
     * an exception supposed to convey an unexpected behavior. Worse you could even throw an exception for a OK
     * ValidationResult.
     * 
     * @param vr
     */
    @Deprecated
    public ComponentException(ValidationResult vr) {
        super(CommonErrorCodes.UNEXPECTED_EXCEPTION,
                ExceptionContext.withBuilder().put("message", vr.getMessage()).put("status", vr.getStatus()).build());
        setValidationResult(vr);
    }

    public ComponentException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public ComponentException(ErrorCode code, ExceptionContext context) {
        super(code, context);
    }

    public ComponentException(ErrorCode code, Throwable cause, ExceptionContext context) {
        super(code, cause, context);
    }

    /**
     * please do not use this to exception convey {@link ValidationResult}, A {@link ValidationResult} is not a
     * unexpected behavior. see {@link #ComponentException(ValidationResult)}
     */
    @Deprecated
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * please do not use this to exception convey {@link ValidationResult}, A {@link ValidationResult} is not a
     * unexpected behavior. see {@link #ComponentException(ValidationResult)}
     */
    @Deprecated
    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }
}
