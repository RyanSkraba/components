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
package org.talend.components.azurestorage.wizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.NamedThing;

public class AzureStorageConnectionWizard extends ComponentWizard {

    TAzureStorageConnectionProperties cProperties;

    AzureStorageComponentListProperties qProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageConnectionWizard.class);

    public AzureStorageConnectionWizard(ComponentWizardDefinition definition, String repositoryLocation) {
        super(definition, repositoryLocation);

        cProperties = new TAzureStorageConnectionProperties("connection");
        cProperties.init();
        cProperties.setRepositoryLocation(repositoryLocation);
        addForm(cProperties.getForm(TAzureStorageConnectionProperties.FORM_WIZARD));

        qProperties = new AzureStorageComponentListProperties("qProperties").setConnection(cProperties)
                .setRepositoryLocation(repositoryLocation);
        qProperties.init();

        addForm(qProperties.getForm(AzureStorageComponentListProperties.FORM_CONTAINER));
        addForm(qProperties.getForm(AzureStorageComponentListProperties.FORM_QUEUE));
        addForm(qProperties.getForm(AzureStorageComponentListProperties.FORM_TABLE));
    }

    public void setupProperties(TAzureStorageConnectionProperties properties) {
        cProperties.setupProperties();
        cProperties.copyValuesFrom(properties);
        if(properties.BlobSchema!=null){
            qProperties.selectedContainerNames.setStoredValue(properties.BlobSchema);
        }
        
        if(properties.QueueSchema!=null){
            qProperties.selectedQueueNames.setStoredValue(properties.QueueSchema);
        }
        
        if(properties.TableSchema!=null){
            qProperties.selectedTableNames.setStoredValue(properties.TableSchema);
        }
        
        qProperties.setConnection(cProperties);
    }

}
