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

package org.talend.components.netsuite.util;

import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * Provides utility methods for component exceptions.
 */
public abstract class ComponentExceptions {

    /**
     * Compose {@code ValidationResult} for given exception.
     *
     * @param ex exception
     * @return {@code ValidationResult}
     */
    public static ValidationResult exceptionToValidationResult(Exception ex) {
        ValidationResult vr = null;
        if (ex instanceof ComponentException) {
            vr = ((ComponentException) ex).getValidationResult();
        }
        if (vr == null) {
            vr = new ValidationResult(Result.ERROR, ex.getMessage());
        }
        return vr;
    }

    /**
     * Compose {@link ComponentException} for given exception
     *
     * @param ex exception
     * @return {@code ComponentException}
     */
    public static ComponentException asComponentExceptionWithValidationResult(Exception ex) {
        if (ex instanceof ComponentException && (((ComponentException) ex).getValidationResult() != null)) {
            return ((ComponentException) ex);
        }
        return new ComponentException(exceptionToValidationResult(ex));
    }

}
