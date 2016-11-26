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
package org.talend.components.service.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.RuntimableDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.service.rest.dto.ConnectorTypology;
import org.talend.components.service.rest.dto.DefinitionDTO;
import org.talend.components.service.rest.mock.MockComponentDefinition;
import org.talend.components.service.rest.mock.MockDatastoreDefinition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.jayway.restassured.RestAssured.when;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.components.api.component.ConnectorTopology.*;
import static org.talend.components.service.rest.DefinitionType.COMPONENT;
import static org.talend.components.service.rest.DefinitionType.DATA_STORE;
import static org.talend.components.service.rest.dto.ConnectorTypology.*;

/**
 * Unit test for the org.talend.components.service.rest.DefinitionsController class.
 *
 * @see DefinitionsController
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = { "server.contextPath=" })
public class DefinitionsControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private DefinitionRegistryService delegate;

    @Autowired
    private DefinitionsController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    @Test
    public void shouldListDatastoreDefinitions() throws Exception {
        shouldListDefinitions(asList("toto", "tutu", "titi"), //
                DatastoreDefinition.class, //
                this::getDatastoreDefinitions, //
                DATA_STORE, //
                "datastore");
    }

    @Test
    public void shouldListComponentDefinitions() throws Exception {
        shouldListDefinitions(asList("one", "two", "three"), //
                ComponentDefinition.class, //
                this::getComponentDefinitions, //
                COMPONENT, //
                "component");
    }

    public void shouldListDefinitions(List<String> names, //
            Class clazz, //
            Function<List<String>, Map<String, ? extends RuntimableDefinition>> provider, //
            DefinitionType wantedType, //
            String expectedType) throws IOException {
        // given
        BDDMockito.given(delegate.getDefinitionsMapByType(clazz)) //
                .willReturn(provider.apply(names));

        // when
        final Response response = when().get("/definitions/" + wantedType).andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(names.size(), actual.size());
        actual.forEach(d -> {
            assertEquals(expectedType, d.getType());
            assertTrue(names.contains(d.getName().substring("mock ".length()))); // it's expected
        });
    }

    private Map<String, DatastoreDefinition> getDatastoreDefinitions(List<String> names) {
        Map<String, DatastoreDefinition> definitions = new HashMap<>();
        for (String name : names) {
            definitions.put(name, new MockDatastoreDefinition(name));
        }
        return definitions;
    }

    private Map<String, ComponentDefinition> getComponentDefinitions(List<String> names) {
        Map<String, ComponentDefinition> definitions = new HashMap<>();
        for (String name : names) {
            definitions.put(name, new MockComponentDefinition(name));
        }
        return definitions;
    }

    @Test
    public void shouldFilterSourceTypology() throws Exception {
        shouldFilterComponentsByTypology(SOURCE, 5); // 3 sources + 2 source & sink
    }

    @Test
    public void shouldFilterSinkTypology() throws Exception {
        shouldFilterComponentsByTypology(SINK, 5); // 3 sources + 2 source & sink
    }

    @Test
    public void shouldFilterTransformerTypology() throws Exception {
        shouldFilterComponentsByTypology(TRANSFORMER, 3); // 3 transformers
    }

    @Test
    public void shouldFilterConfigurationTypology() throws Exception {
        shouldFilterComponentsByTypology(CONFIGURATION, 3); // 3 configuration
    }

    @Test
    public void shouldNotFilterTypology() throws Exception {
        // given
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();

        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // when
        final Response response = when().get("/definitions/components").andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(14, actual.size());
    }

    public void shouldFilterComponentsByTypology(ConnectorTypology wantedTypology, int expectedResults) throws IOException {
        // given
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();

        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // when
        final Response response = when().get("/definitions/components?typology=" + wantedTypology.name()).andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(expectedResults, actual.size());
        assertEquals(expectedResults, actual.stream().filter(dto -> dto.getTypologies().contains(wantedTypology.name())) // it's a
                                                                                                                         // source
                .count());
    }

    private Map<String, ComponentDefinition> getComponentsDefinitions() {
        Map<String, ComponentDefinition> definitions = new HashMap<>();
        definitions.put("source_1", new MockComponentDefinition("source_1", INCOMING));
        definitions.put("source_2", new MockComponentDefinition("source_2", INCOMING));
        definitions.put("source_3", new MockComponentDefinition("source_2", INCOMING));
        definitions.put("sink_1", new MockComponentDefinition("sink_1", OUTGOING));
        definitions.put("sink_2", new MockComponentDefinition("sink_2", OUTGOING));
        definitions.put("sink_3", new MockComponentDefinition("sink_3", OUTGOING));
        definitions.put("transformer_1", new MockComponentDefinition("transformer_1", INCOMING_AND_OUTGOING));
        definitions.put("transformer_2", new MockComponentDefinition("transformer_2", INCOMING_AND_OUTGOING));
        definitions.put("transformer_3", new MockComponentDefinition("transformer_3", INCOMING_AND_OUTGOING));
        definitions.put("config_1", new MockComponentDefinition("config_1", NONE));
        definitions.put("config_2", new MockComponentDefinition("config_2", NONE));
        definitions.put("config_3", new MockComponentDefinition("config_3", NONE));
        definitions.put("s&s_1", new MockComponentDefinition("config_3", INCOMING, OUTGOING));
        definitions.put("s&s_2", new MockComponentDefinition("config_3", INCOMING, OUTGOING));
        return definitions;
    }
}