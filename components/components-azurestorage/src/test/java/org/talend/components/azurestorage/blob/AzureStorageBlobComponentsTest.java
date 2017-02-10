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
package org.talend.components.azurestorage.blob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties.AccessControl;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerdelete.TAzureStorageContainerDeleteProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainerlist.TAzureStorageContainerListProperties;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteDefinition;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteProperties;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetDefinition;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListDefinition;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.components.azurestorage.blob.tazurestorageput.TAzureStoragePutDefinition;
import org.talend.components.azurestorage.blob.tazurestorageput.TAzureStoragePutProperties;
import org.talend.daikon.properties.presentation.Form;

public class AzureStorageBlobComponentsTest {

    private AzureStorageContainerDefinition azureStorageContainerDefinition;

    @Before
    public void setUp() throws Exception {
        azureStorageContainerDefinition = new TAzureStorageContainerListDefinition();
    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() {
        assertNotNull(azureStorageContainerDefinition.getNestedCompatibleComponentPropertiesClass());
    }

    @Test
    public void testGetFamilies() {
        assertNotNull(azureStorageContainerDefinition.getFamilies());
    }

    @Test
    public void testGetRuntimeInfo() {
        assertNotNull(azureStorageContainerDefinition.getRuntimeInfo(null, null, ConnectorTopology.OUTGOING));
        assertNull(azureStorageContainerDefinition.getRuntimeInfo(null, null, ConnectorTopology.INCOMING));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        assertEquals(EnumSet.of(ConnectorTopology.OUTGOING),
                new TAzureStorageContainerListDefinition().getSupportedConnectorTopologies());
        assertEquals(EnumSet.of(ConnectorTopology.OUTGOING), new TAzureStorageListDefinition().getSupportedConnectorTopologies());
    }

    @Test
    public void testIsSchemAutoPropagate() {
        assertTrue(new TAzureStorageContainerListDefinition().isSchemaAutoPropagate());
        assertTrue(new TAzureStorageListDefinition().isSchemaAutoPropagate());
    }

    @Test
    public void testGetPropertyClass() {
        assertEquals(TAzureStorageContainerExistProperties.class, new TAzureStorageContainerExistDefinition().getPropertyClass());
        assertEquals(TAzureStorageContainerListProperties.class, new TAzureStorageContainerListDefinition().getPropertyClass());
        assertEquals(TAzureStorageContainerCreateProperties.class,
                new TAzureStorageContainerCreateDefinition().getPropertyClass());
        assertEquals(TAzureStorageContainerDeleteProperties.class,
                new TAzureStorageContainerDeleteDefinition().getPropertyClass());
        assertEquals(TAzureStorageDeleteProperties.class, new TAzureStorageDeleteDefinition().getPropertyClass());
        assertEquals(TAzureStorageGetProperties.class, new TAzureStorageGetDefinition().getPropertyClass());
        assertEquals(TAzureStorageListProperties.class, new TAzureStorageListDefinition().getPropertyClass());
        assertEquals(TAzureStoragePutProperties.class, new TAzureStoragePutDefinition().getPropertyClass());
    }

    @Test
    public void testEnums() {
        assertEquals(AccessControl.Private, AccessControl.valueOf("Private"));
        assertEquals(AccessControl.Public, AccessControl.valueOf("Public"));
    }

    @Test
    public void testTAzureStoragePutProperties() {
        TAzureStoragePutProperties p = new TAzureStoragePutProperties("test");
        p.setupProperties();
        p.connection.setupProperties();
        p.connection.setupLayout();
        p.setupLayout();
        p.useFileList.setValue(true);
        p.afterUseFileList();
        p.refreshLayout(p.getForm(Form.MAIN));
        assertTrue(p.getForm(Form.MAIN).getWidget(p.files.getName()).isVisible());
    }

}
