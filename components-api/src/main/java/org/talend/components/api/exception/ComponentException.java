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
package org.talend.components.api.exception;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * created by sgandon on 9 sept. 2015 Detailled comment
 *
 */
public class ComponentException extends TalendRuntimeException {

    public ComponentException(ErrorCode code) {
        super(code);
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
     * 
     */
    private static final long serialVersionUID = -84662653622272070L;
    // FIXME this should be similar to dataprep Exception, be will be gathered into a common back-end project
}
