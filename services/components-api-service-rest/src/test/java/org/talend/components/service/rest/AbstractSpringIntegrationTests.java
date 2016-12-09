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

package org.talend.components.service.rest;

import java.util.HashMap;
import java.util.Map;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.RuntimableDefinition;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.service.rest.mock.MockDatasetDefinition;
import org.talend.components.service.rest.mock.MockDatasetProperties;
import org.talend.components.service.rest.mock.MockDatastoreDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Created so that integration tests shares the same spring context instead of recreating it each time.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = { "server.contextPath=" })
public abstract class AbstractSpringIntegrationTests {

    @LocalServerPort
    protected int localServerPort;

    @MockBean
    protected DefinitionRegistryService delegate;

    protected static final String DATA_STORE_DEFINITION_NAME = "data store definition name";

    protected static final String DATA_SET_DEFINITION_NAME = "dataset definition name";

    @Before
    public void setUp() {
        // ensure any call from restassured goes to our server isntance
        RestAssured.port = localServerPort;

        // Init the mock delegate to return our data store mock on demand
        MockDatastoreDefinition datastoreDefinition = new MockDatastoreDefinition(DATA_STORE_DEFINITION_NAME);
        MockDatasetDefinition datasetDefinition = new MockDatasetDefinition(DATA_SET_DEFINITION_NAME);

        Map<String, DatastoreDefinition> datastoresMap = singletonMap(DATA_STORE_DEFINITION_NAME, datastoreDefinition);
        when(delegate.getDefinitionsMapByType(DatastoreDefinition.class)) //
                .thenReturn(datastoresMap);

        Map<String, DatasetDefinition> datasetMap = singletonMap(DATA_SET_DEFINITION_NAME, datasetDefinition);
        when(delegate.getDefinitionsMapByType(DatasetDefinition.class)) //
                .thenReturn(datasetMap);

        Map<String, RuntimableDefinition> runtimablesMap = new HashMap<>();
        runtimablesMap.putAll(datastoresMap);
        runtimablesMap.putAll(datasetMap);
        when(delegate.getDefinitionsMapByType(RuntimableDefinition.class)) //
                .thenReturn(runtimablesMap);
        when(delegate.getDefinitionsMapByType(Definition.class)) //
                .thenReturn((Map) runtimablesMap);

        // TODO: map the dataset definition on the correct name

        when(delegate.getDefinitionForPropertiesType(MockDatasetProperties.class)).thenReturn(singletonList(datasetDefinition));

        when(delegate.createProperties(any(Definition.class), anyString())).thenAnswer(i -> {
            Properties properties = PropertiesImpl.createNewInstance(
                    ((Definition<Properties>) i.getArguments()[0]).getPropertiesClass(), (String) i.getArguments()[1]);
            properties.init();
            return properties;
        });
    }

}
