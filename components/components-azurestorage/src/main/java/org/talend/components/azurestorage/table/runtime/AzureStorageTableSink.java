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
package org.talend.components.azurestorage.table.runtime;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageTableSink extends AzureStorageTableSourceOrSink implements Sink {

    private static final long serialVersionUID = -3926228821855368697L;
    
    TAzureStorageOutputTableProperties tableOutputProperties;
    
    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageTableSink.class);

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new AzureStorageTableWriteOperation(this);
    }

    public TAzureStorageOutputTableProperties getProperties() {
        return (TAzureStorageOutputTableProperties) properties;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validationResult = super.validate(container);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }
        this.tableOutputProperties = (TAzureStorageOutputTableProperties) properties;
        
        tableOutputProperties.updatePartitionKeyAndRowKey();
        
        if (!tableOutputProperties.partitionKey.getPossibleValues().contains(tableOutputProperties.partitionKey.getStringValue())
                || !tableOutputProperties.rowKey.getPossibleValues().contains(tableOutputProperties.rowKey.getStringValue())) {
            return new ValidationResult(ValidationResult.Result.ERROR, i18nMessages.getMessage("error.invalidPartitionOrRowKey"));
        }
        if (tableOutputProperties.partitionKey.getStringValue().equals(tableOutputProperties.rowKey.getStringValue())) {
            return new ValidationResult(ValidationResult.Result.ERROR, i18nMessages.getMessage("error.equalPartitionOrRowKey"));
        }

        return ValidationResult.OK;
    }
    
    
    
    
}
