//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.dto;

import java.util.Collection;
import java.util.List;

import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.talend.daikon.properties.ValidationResult.Result.ERROR;
import static org.talend.daikon.properties.ValidationResult.Result.OK;

public class ValidationResultsDto {

    private final List<ValidationResultDto> results;

    private final Result status;

    public ValidationResultsDto(ValidationResult result) {
        this(singletonList(result));
    }

    public ValidationResultsDto(Collection<ValidationResult> results) {
        this.results = results.stream().map(ValidationResultDto::from).collect(toList());
        this.status = results.stream().map(ValidationResult::getStatus).anyMatch(ERROR::equals) ? ERROR : OK;
    }

    public List<ValidationResultDto> getResults() {
        return results;
    }

    public Result getStatus() {
        return status;
    }
}
