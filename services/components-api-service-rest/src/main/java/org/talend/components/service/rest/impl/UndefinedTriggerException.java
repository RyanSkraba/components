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

package org.talend.components.service.rest.impl;

import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.DefaultErrorCode;
import org.talend.daikon.serialize.jsonschema.PropertyTrigger;

/**
 * Thrown when an application asks for a trigger on a component {@link org.talend.daikon.properties.Properties} that does not have
 * the trigger for the asked property.
 */
public class UndefinedTriggerException extends TalendRuntimeException {

    public UndefinedTriggerException(String definition, String property, PropertyTrigger trigger) {
        super(new TriggerDoesNotExistErrorCode(),
                ExceptionContext.build().put("definition", definition).put("property", property).put("trigger", trigger));
    }

    private static class TriggerDoesNotExistErrorCode extends DefaultErrorCode {

        public TriggerDoesNotExistErrorCode() {
            super(HttpStatus.BAD_REQUEST.value(), "definition", "property", "trigger");
        }

        @Override
        public String getProduct() {
            return "TCOMP";
        }

    }
}
