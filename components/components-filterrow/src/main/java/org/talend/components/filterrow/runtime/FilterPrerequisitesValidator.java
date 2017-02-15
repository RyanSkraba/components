// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow.runtime;

import java.text.MessageFormat;
import java.util.Collection;

import org.apache.avro.Schema;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.OperatorType;
import org.talend.components.filterrow.processing.ProcessingHelper;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * created by dmytro.chmyga on Dec 23, 2016
 */
public class FilterPrerequisitesValidator {

    public ValidationResult validate(FunctionType functionType, OperatorType operatorType, Schema.Type fieldType,
            Object predefinedValue) {
        if (fieldType == Schema.Type.STRING) {
            Collection<OperatorType> possibleOperators = ProcessingHelper.getPossibleOperatorTypesByFunctionType(functionType);
            if (!possibleOperators.contains(operatorType)) {
                return new ValidationResult().setStatus(Result.ERROR)
                        .setMessage(MessageFormat.format("Possible operators for {0} are {1}", functionType, possibleOperators));
            }
            if (functionType == FunctionType.LENGTH && !(predefinedValue instanceof Number)) {
                return new ValidationResult().setStatus(Result.ERROR)
                        .setMessage(MessageFormat.format("Predefined value for {0} must be numeric", functionType));
            }
        } else {
            if (functionType != FunctionType.EMPTY) {
                return new ValidationResult().setStatus(Result.ERROR)
                        .setMessage(MessageFormat.format("Function for this field type can be only {0}", FunctionType.EMPTY));
            }
            if (ProcessingHelper.getComparableOperatorTypes().contains(functionType)
                    && !(predefinedValue instanceof Comparable)) {
                return new ValidationResult().setStatus(Result.ERROR)
                        .setMessage("Predefined value for this operator must be Comparable");
            }
        }
        return ValidationResult.OK;
    }

}
