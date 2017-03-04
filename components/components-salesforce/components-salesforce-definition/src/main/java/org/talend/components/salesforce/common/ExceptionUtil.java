package org.talend.components.salesforce.common;

import org.talend.daikon.properties.ValidationResult;

public class ExceptionUtil {

    public static ValidationResult exceptionToValidationResult(Exception ex) {
        ValidationResult vr = new ValidationResult();
        // FIXME - do a better job here
        vr.setMessage(ex.getMessage());
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }
    
}
