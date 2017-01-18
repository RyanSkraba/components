//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.impl;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataStoreServiceImplTest {

    @Mock
    private ComponentService componentServiceDelegate;

    @Mock
    private DefinitionRegistryService defRegistryDelegate;

    @Test
    @Ignore
    public void listDataStoreDefinitions() throws Exception {
        ArrayList<DatastoreDefinition> value = new ArrayList<>();
        when(defRegistryDelegate.getDefinitionsMapByType(DatastoreDefinition.class).values()).thenReturn(value);

        //Iterable<DefinitionDTO> datastoreDefinitions = dataStoreController.listDataStoreDefinitions(
          //      DefinitionType.DATA_STORE);

        // assertEquals(value, datastoreDefinitions);
        // verify(componentServiceDelegate).getDefinitionsByType(DatastoreDefinition.class);
    }

    @Test
    @Ignore
    public void getDatastoreDefinition() throws Exception {
        // Given
        ArrayList<DatastoreDefinition> definitions = new ArrayList<>();
        DatastoreDefinition dsd1 = mock(DatastoreDefinition.class);
        when(dsd1.getName()).thenReturn("toto");
        definitions.add(dsd1);
        DatastoreDefinition dsd2 = mock(DatastoreDefinition.class);
        String datastoreName = "datastore name";
        when(dsd2.getName()).thenReturn(datastoreName);
        definitions.add(dsd2);
        when(defRegistryDelegate.getDefinitionsMapByType(DatastoreDefinition.class).values()).thenReturn(definitions);

        // When
        // DatastoreDefinition datastoreDefinition = dataStoreController.getDataStoreProperties(datastoreName);
        //
        // // Then
        // assertEquals(dsd2, datastoreDefinition);
        // verify(componentServiceDelegate, times(1)).getDefinitionsByType(DatastoreDefinition.class);
        // verify(dsd1, times(1)).getName();
        // verify(dsd2, times(1)).getName();
    }

    @Test
    @Ignore
    public void validateDatastoreDefinition() throws Exception {

    }

}
