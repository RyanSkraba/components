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
package org.talend.components.azurestorage;

import org.osgi.service.component.annotations.Component;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListDefinition;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteDefinition;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetDefinition;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListDefinition;
import org.talend.components.azurestorage.blob.tazurestorageput.TAzureStoragePutDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuedelete.TAzureStorageQueueDeleteDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuepurge.TAzureStorageQueuePurgeDefinition;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableDefinition;
import org.talend.components.azurestorage.table.tazurestorageoutputtable.TAzureStorageOutputTableDefinition;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionDefinition;
import org.talend.components.azurestorage.wizard.AzureStorageConnectionEditWizardDefinition;
import org.talend.components.azurestorage.wizard.AzureStorageConnectionWizardDefinition;

/**
 * Install all of the definitions provided for the Azure Storage family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + AzureStorageFamilyDefinition.NAME, service = ComponentInstaller.class)
public class AzureStorageFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Azure Storage"; //$NON-NLS-1$

    public AzureStorageFamilyDefinition() {
        super(NAME, new TAzureStorageConnectionDefinition(),
                // containers and blobs
                new TAzureStorageContainerExistDefinition(), new TAzureStorageContainerCreateDefinition(),
                new TAzureStorageContainerDeleteDefinition(), new TAzureStorageContainerListDefinition(),
                new TAzureStorageListDefinition(), new TAzureStorageDeleteDefinition(), new TAzureStorageGetDefinition(),
                new TAzureStoragePutDefinition(),
                // tables
                new TAzureStorageInputTableDefinition(), new TAzureStorageOutputTableDefinition(),
                // queues
                new TAzureStorageQueueCreateDefinition(), new TAzureStorageQueueDeleteDefinition(),
                new TAzureStorageQueueListDefinition(), new TAzureStorageQueueInputDefinition(),
                new TAzureStorageQueueOutputDefinition(), new TAzureStorageQueuePurgeDefinition(),
                new TAzureStorageQueueInputLoopDefinition(),
                // wizards
                new AzureStorageConnectionWizardDefinition(), new AzureStorageConnectionEditWizardDefinition()
        //
        );
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}