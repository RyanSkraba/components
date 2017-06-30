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
package org.talend.components.azurestorage.blob.definitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties.AccessControl;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListDefinition;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListDefinition;

public abstract class AzureStorageContainerDefinitionTest {

    protected AzureStorageContainerDefinition azureStorageContainerDefinition;

    /**
     * initialize azureStorageContainerDefinition
     * 
     */
    @Before
    abstract public void setUp() throws Exception;

    @Test
    abstract public void testGetPropertiesClass();

    @Test
    abstract public void testGetSupportedConnectorTopologies();

    @Test
    public abstract void testGetRuntimeInfo();

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() {
        assertNotNull(azureStorageContainerDefinition.getNestedCompatibleComponentPropertiesClass());
    }

    @Test
    public void testGetFamilies() {
        assertNotNull(azureStorageContainerDefinition.getFamilies());
    }

    @Test
    public abstract void testGetReturnProperties();

    @Test
    public void testIsSchemAutoPropagate() {
        assertTrue(new TAzureStorageContainerListDefinition().isSchemaAutoPropagate());
        assertTrue(new TAzureStorageListDefinition().isSchemaAutoPropagate());
    }

    @Test
    public void testEnums() {
        assertEquals(AccessControl.Private, AccessControl.valueOf("Private"));
        assertEquals(AccessControl.Public, AccessControl.valueOf("Public"));
    }

}
