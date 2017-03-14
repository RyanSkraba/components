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

package org.talend.components.netsuite.client;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.exception.json.JsonErrorCode;

/**
 *
 */
public class NetSuiteException extends TalendRuntimeException {

    public NetSuiteException(String message) {
        super(CommonErrorCodes.UNEXPECTED_EXCEPTION,
                ExceptionContext.build().put(ExceptionContext.KEY_MESSAGE, message));
    }

    public NetSuiteException(String message, Throwable cause) {
        super(CommonErrorCodes.UNEXPECTED_EXCEPTION, cause,
                ExceptionContext.build().put(ExceptionContext.KEY_MESSAGE, message));
    }

    public NetSuiteException(ErrorCode code, Throwable cause, ExceptionContext context) {
        super(code, cause, context);
    }

    public NetSuiteException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }

    public NetSuiteException(ErrorCode code, ExceptionContext context) {
        super(code, context);
    }

    public NetSuiteException(JsonErrorCode code) {
        super(code);
    }

    public NetSuiteException(ErrorCode code) {
        super(code);
    }
}
