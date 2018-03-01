//==============================================================================
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
//==============================================================================
package org.talend.components.marklogic.runtime.bulkload;

import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;

public class MarkLogicBulkLoad implements ComponentDriverInitialization {

    private static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarkLogicBulkLoad.class);

    private MarkLogicBulkLoadProperties bulkLoadProperties;

    @Override
    public void runAtDriver(RuntimeContainer container) {
        AbstractMarkLogicBulkLoadRunner bulkLoadRunner;

        bulkLoadRunner = bulkLoadProperties.useExternalMLCP.getValue() ?
                new MarkLogicExternalBulkLoadRunner(bulkLoadProperties) :
                new MarkLogicInternalBulkLoadRunner(bulkLoadProperties);

        bulkLoadRunner.performBulkLoad();
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, Properties properties) {
        ValidationResultMutable validationResult = new ValidationResultMutable();
        if (properties instanceof MarkLogicBulkLoadProperties) {
            bulkLoadProperties = (MarkLogicBulkLoadProperties) properties;

            if (isRequiredPropertiesMissed()) {
                validationResult.setStatus(ValidationResult.Result.ERROR);
                validationResult.setMessage(MESSAGES.getMessage("error.missedProperties"));
            }

        } else {
            validationResult.setStatus(ValidationResult.Result.ERROR);
            validationResult.setMessage(MESSAGES.getMessage("error.wrongProperties"));
        }

        return validationResult;
    }

    private boolean isRequiredPropertiesMissed() {
        MarkLogicConnectionProperties connection = bulkLoadProperties.getConnection();
        return connection.host.getStringValue().isEmpty() || connection.port.getValue() == null || connection.database
                .getStringValue().isEmpty() || connection.username.getStringValue().isEmpty() || connection.password
                .getStringValue().isEmpty() || bulkLoadProperties.loadFolder.getStringValue().isEmpty();
    }
}
