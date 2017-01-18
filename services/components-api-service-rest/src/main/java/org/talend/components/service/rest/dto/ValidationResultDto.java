//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.dto;

import org.talend.daikon.properties.ValidationResult;

public class ValidationResultDto {

    public final ValidationResult.Result status;

    public final String message;

    public ValidationResultDto(ValidationResult.Result status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ValidationResultDto from(ValidationResult validationResult) {
        return new ValidationResultDto(validationResult.getStatus(), validationResult.getMessage());
    }

    public ValidationResult.Result getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
