// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.exceptions;

import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.exception.error.ErrorCode;

public class MarkLogicException extends ComponentException {

    public MarkLogicException(ErrorCode code) {
        super(code);
    }

    public MarkLogicException(ErrorCode code, Throwable cause) {
        super(code, cause);
    }
}
