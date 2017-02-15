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

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest2;
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
import org.talend.daikon.definition.service.DefinitionRegistryService;

public abstract class AzureStorageGenericBase extends AbstractComponentTest2 {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Inject
    DefinitionRegistry testComponentRegistry;

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        if (testComponentRegistry == null) {
            testComponentRegistry = new DefinitionRegistry();

            testComponentRegistry.registerComponentFamilyDefinition(new AzureStorageFamilyDefinition());
        }
        return testComponentRegistry;
    }

    // @Inject
    // ComponentService compServ;

    private ComponentServiceImpl componentService;

    public ComponentService getComponentService() {
        // return compServ;
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();

            testComponentRegistry.registerComponentFamilyDefinition(new AzureStorageFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void testAllComponentsAreRegistered() {
        // blobs
        assertComponentIsRegistered(TAzureStorageConnectionDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageContainerCreateDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageContainerDeleteDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageContainerExistDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageContainerListDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageDeleteDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageGetDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageListDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStoragePutDefinition.COMPONENT_NAME);
        // tables
        assertComponentIsRegistered(TAzureStorageInputTableDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageOutputTableDefinition.COMPONENT_NAME);
        // queues
        assertComponentIsRegistered(TAzureStorageQueueInputDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageQueueOutputDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageQueuePurgeDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageQueueDeleteDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageQueueCreateDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageQueueListDefinition.COMPONENT_NAME);
        assertComponentIsRegistered(TAzureStorageQueueInputLoopDefinition.COMPONENT_NAME);
    }
}
