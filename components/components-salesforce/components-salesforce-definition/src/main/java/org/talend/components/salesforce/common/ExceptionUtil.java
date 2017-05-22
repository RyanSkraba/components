package org.talend.components.salesforce.common;

import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;

public class ExceptionUtil {

    public static ValidationResult exceptionToValidationResult(Exception ex) {
        ValidationResultMutable vr = new ValidationResultMutable();
        // FIXME - do a better job here
        vr.setMessage(ex.getMessage());
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }
    
}
