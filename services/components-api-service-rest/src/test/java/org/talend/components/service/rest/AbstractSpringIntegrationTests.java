// ==============================================================================
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
// ==============================================================================

package org.talend.components.service.rest;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.service.rest.dto.SerPropertiesDto;
import org.talend.components.service.rest.dto.UiSpecsPropertiesDto;
import org.talend.components.service.rest.impl.PropertiesHelpers;
import org.talend.components.service.rest.mock.MockDatasetDefinition;
import org.talend.components.service.rest.mock.MockDatasetProperties;
import org.talend.components.service.rest.mock.MockDatastoreDefinition;
import org.talend.components.service.rest.mock.MockDatastoreProperties;
import org.talend.components.service.rest.serialization.JsonSerializationHelper;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created so that integration tests shares the same spring context instead of recreating it each time.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = { "server.contextPath=" })
public abstract class AbstractSpringIntegrationTests {
    static {
        if (System.getProperty("sun.boot.class.path") == null) {
            System.setProperty("sun.boot.class.path", System.getProperty("java.class.path"));
        }
    }

    @LocalServerPort
    protected int localServerPort;

    @MockBean
    protected DefinitionRegistryService delegate;

    @Inject
    JsonSerializationHelper jsonHelper;

    @Inject
    PropertiesHelpers propsHelper;

    protected static final String DATA_STORE_DEFINITION_NAME = "MockDatastore";

    protected static final String DATA_SET_DEFINITION_NAME = "MockDataset";

    public static final String TEST_DATA_STORE_PROPERTIES = "{\"@definitionName\":\"" + DATA_STORE_DEFINITION_NAME
            + "\",\"tag\":\"tata\", \"tagId\":256}";

    public static final String TEST_DATA_SET_PROPERTIES = "{\"@definitionName\":\"" + DATA_SET_DEFINITION_NAME
            + "\",\"tag\":\"tata\", \"tagId\":256}";

    protected ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        if (System.getProperty("sun.boot.class.path") == null) { // daikon workaround for j11
            System.setProperty("sun.boot.class.path", "");
        }

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

        Map<String, Definition> runtimablesMap = new HashMap<>();
        runtimablesMap.putAll(datastoresMap);
        runtimablesMap.putAll(datasetMap);
        when(delegate.getDefinitionsMapByType(Definition.class)) //
                .thenReturn(runtimablesMap);
        when(delegate.getDefinitionsMapByType(Definition.class)) //
                .thenReturn(runtimablesMap);

        // TODO: map the dataset definition on the correct name

        when(delegate.getDefinitionForPropertiesType(MockDatasetProperties.class)).thenReturn(singletonList(datasetDefinition));
        when(delegate.getDefinitionForPropertiesType(MockDatastoreProperties.class))
                .thenReturn(singletonList(datastoreDefinition));

        when(delegate.createProperties(any(Definition.class), anyString())).thenAnswer(i -> {
            Properties properties = PropertiesImpl.createNewInstance(
                    ((Definition<Properties>) i.getArguments()[0]).getPropertiesClass(), (String) i.getArguments()[1]);
            properties.init();
            return properties;
        });
    }

    protected UiSpecsPropertiesDto buildTestDataSetFormData() throws java.io.IOException {
        UiSpecsPropertiesDto formDataContainer = new UiSpecsPropertiesDto();
        ObjectReader reader = mapper.readerFor(ObjectNode.class);
        formDataContainer.setDependencies(singletonList(reader.readValue(TEST_DATA_STORE_PROPERTIES)));
        formDataContainer.setProperties(reader.readValue(TEST_DATA_SET_PROPERTIES));
        return formDataContainer;
    }

    protected UiSpecsPropertiesDto buildTestDataStoreFormData() throws java.io.IOException {
        UiSpecsPropertiesDto formDataContainer = new UiSpecsPropertiesDto();
        formDataContainer.setProperties(mapper.readerFor(ObjectNode.class).readValue(TEST_DATA_STORE_PROPERTIES));
        return formDataContainer;
    }

    protected SerPropertiesDto buildTestDataStoreSerProps() throws java.io.IOException {
        SerPropertiesDto formDataContainer = new SerPropertiesDto();
        formDataContainer.setProperties(new MockDatastoreProperties("").init().toSerialized());
        return formDataContainer;
    }

    protected SerPropertiesDto buildTestDataSetSerProps() throws java.io.IOException {
        SerPropertiesDto formDataContainer = new SerPropertiesDto();
        MockDatasetProperties mockDatasetProperties = new MockDatasetProperties("foo");
        mockDatasetProperties.tag.setValue("tata");
        mockDatasetProperties.tagId.setValue(256);
        MockDatastoreProperties mockDatastoreProperties = new MockDatastoreProperties("bar");
        formDataContainer.setDependencies(singletonList(mockDatastoreProperties.toSerialized()));
        formDataContainer.setProperties(mockDatasetProperties.toSerialized());
        return formDataContainer;
    }

    protected String getMockDatasetMainFormUISpecs() throws IOException {
        Properties properties = propsHelper.propertiesFromDto(buildTestDataSetFormData());
        return jsonHelper.toJson(Form.MAIN, properties);
    }

}
