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
package org.talend.components.azurestorage.blob.runtime;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageContainerRuntime extends AzureStorageRuntime {

    private static final long serialVersionUID = 312081420617929183L;

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageContainerCreateRuntime.class);

    protected String containerName;

    protected boolean dieOnError;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        // init
        AzureStorageContainerProperties componentProperties = (AzureStorageContainerProperties) properties;

        this.containerName = componentProperties.container.getValue();

        // validate
        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        String errorMessage = "";
        // not empty
        if (StringUtils.isEmpty(containerName)) {
            errorMessage = messages.getMessage("error.ContainerEmpty");
        }
        // valid characters 0-9 a-z and -
        else if (!StringUtils.isAlphanumeric(containerName.replaceAll("-", ""))) {

            errorMessage = messages.getMessage("error.IncorrectName");
        }
        // all lowercase
        else if (!StringUtils.isAllLowerCase(containerName.replaceAll("(-|\\d)", ""))) {
            errorMessage = messages.getMessage("error.UppercaseName");
        }
        // length range : 3-63
        else if ((containerName.length() < 3) || (containerName.length() > 63)) {
            errorMessage = messages.getMessage("error.LengthError");
        }

        if (errorMessage.isEmpty()) {
            return ValidationResult.OK;
        } else {
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
    }

}
