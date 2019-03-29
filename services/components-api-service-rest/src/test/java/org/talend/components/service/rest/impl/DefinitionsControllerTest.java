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
package org.talend.components.service.rest.impl;

import static io.restassured.RestAssured.when;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.components.api.component.ConnectorTopology.INCOMING;
import static org.talend.components.api.component.ConnectorTopology.INCOMING_AND_OUTGOING;
import static org.talend.components.api.component.ConnectorTopology.NONE;
import static org.talend.components.api.component.ConnectorTopology.OUTGOING;
import static org.talend.components.service.rest.DefinitionType.COMPONENT;
import static org.talend.components.service.rest.DefinitionType.DATA_STORE;
import static org.talend.components.service.rest.dto.ConnectorTypology.CONFIGURATION;
import static org.talend.components.service.rest.dto.ConnectorTypology.SINK;
import static org.talend.components.service.rest.dto.ConnectorTypology.SOURCE;
import static org.talend.components.service.rest.dto.ConnectorTypology.TRANSFORMER;

import io.restassured.response.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;
import org.talend.components.service.rest.ServiceConstants;
import org.talend.components.service.rest.DefinitionType;
import org.talend.components.service.rest.DefinitionsController;
import org.talend.components.service.rest.dto.ConnectorTypology;
import org.talend.components.service.rest.dto.DefinitionDTO;
import org.talend.components.service.rest.mock.MockComponentDefinition;
import org.talend.components.service.rest.mock.MockDatastoreDefinition;
import org.talend.daikon.definition.Definition;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the org.talend.components.service.rest.DefinitionsController class.
 *
 * @see DefinitionsController
 */
public class DefinitionsControllerTest extends AbstractSpringIntegrationTests {

    @Autowired
    private ObjectMapper objectMapper;

    protected String getVersionPrefix() {
        return ServiceConstants.V0;
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
            Function<List<String>, Map<String, ? extends Definition>> provider, //
            DefinitionType wantedType, //
            String expectedType) throws IOException {
        // given
        BDDMockito.given(delegate.getDefinitionsMapByType(clazz)) //
                .willReturn(provider.apply(names));

        // when
        final Response response = when().get(getVersionPrefix() + "/definitions/" + wantedType).andReturn();

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
    public void shouldFilterDIExecutionEngine() throws Exception {
        shouldFilterComponentsByExecutionEngine(ExecutionEngine.DI, 8);
    }

    @Test
    public void shouldFilterBEAMExecutionEngine() throws Exception {
        shouldFilterComponentsByExecutionEngine(ExecutionEngine.BEAM, 1);
    }

    @Test
    public void shouldFilterSparkBatchExecutionEngine() throws Exception {
        shouldFilterComponentsByExecutionEngine(ExecutionEngine.DI_SPARK_BATCH, 2);
    }

    @Test
    public void shouldFilterSparkStreamingExecutionEngine() throws Exception {
        shouldFilterComponentsByExecutionEngine(ExecutionEngine.DI_SPARK_STREAMING, 3);
    }

    @Test
    public void shouldFilterSourceTypologyAndDIExecutionEngine() throws Exception {
        // 1 sources (two of the three OUTGOING sources is BEAM compatible) + 2 source & sink
        shouldFilterComponentsByTypologyAndExecutionEngine(SOURCE, ExecutionEngine.DI, 3);
    }

    @Test
    public void shouldHaveIconKeyIfPresent() throws Exception {
        // given
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();
        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // then
        when().get(getVersionPrefix() + "/definitions/components").then() //
                .statusCode(OK.value()) //
                .body("iconKey", hasSize(14)) // total including nulls
                .body("iconKey.findAll { iconKey -> iconKey != null }", hasSize(5)); // total non-null
    }

    @Test
    public void shouldNotFilterTypology() throws Exception {
        // given
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();

        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // when
        final Response response = when().get(getVersionPrefix() + "/definitions/components").andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(14, actual.size());
    }

    @Test
    public void shouldFilterByTagSource() throws JsonParseException, JsonMappingException, IOException {
        shouldFilterComponentsByTag("source", 3);
    }

    public void shouldFilterComponentsByTypology(ConnectorTypology wantedTypology, int expectedResults) throws IOException {
        // given
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();

        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // when
        final Response response = when().get(getVersionPrefix() + "/definitions/components?typology=" + wantedTypology.name())
                .andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(expectedResults, actual.size());
        assertEquals(expectedResults, actual.stream().filter(dto -> dto.getTypologies().contains(wantedTypology.name())) // it's
                                                                                                                         // a
                                                                                                                         // source
                .count());
    }

    public void shouldFilterComponentsByTag(String tag, int expectedResults)
            throws JsonParseException, JsonMappingException, IOException {
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();

        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // when
        final Response response = when().get(getVersionPrefix() + "/definitions/" + COMPONENT + "?tag=" + tag).andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(expectedResults, actual.size());
    }

    public void shouldFilterComponentsByExecutionEngine(ExecutionEngine executionEngine, int expectedResults) throws IOException {
        // given
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();

        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // when
        final Response response = when()
                .get(getVersionPrefix() + "/definitions/components?executionEngine=" + executionEngine.name()).andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(expectedResults, actual.size());
        assertEquals(expectedResults,
                actual.stream().filter(dto -> dto.getExecutionEngines().contains(executionEngine.name())).count());
    }

    public void shouldFilterComponentsByTypologyAndExecutionEngine(ConnectorTypology wantedTypology,
            ExecutionEngine executionEngine, int expectedResults) throws IOException {
        // given
        Map<String, ComponentDefinition> definitions = getComponentsDefinitions();

        BDDMockito.given(delegate.getDefinitionsMapByType(ComponentDefinition.class)) //
                .willReturn(definitions);

        // when
        final Response response = when().get(getVersionPrefix() + "/definitions/components?typology=" + wantedTypology
                + "&executionEngine=" + executionEngine.name()).andReturn();

        // then
        assertEquals(OK.value(), response.getStatusCode());
        List<DefinitionDTO> actual = objectMapper.readValue(response.asInputStream(), new TypeReference<List<DefinitionDTO>>() {
        });
        assertEquals(expectedResults, actual.size());
        assertEquals(expectedResults, actual.stream().filter(dto -> dto.getTypologies().contains(wantedTypology.name()))
                .filter(dto -> dto.getExecutionEngines().contains(executionEngine.name())).count());
    }

    private Map<String, ComponentDefinition> getComponentsDefinitions() {
        Map<String, ComponentDefinition> definitions = new HashMap<>();
        definitions.put("source_1", new MockComponentDefinition("source_1", "icon_key_source_1", INCOMING));
        definitions.put("source_2", new MockComponentDefinition("source_2", INCOMING));
        definitions.put("source_3", new MockComponentDefinition("source_2", ExecutionEngine.BEAM, INCOMING));
        definitions.put("sink_1", new MockComponentDefinition("sink_1", "icon_key_sink_1", OUTGOING));
        definitions.put("sink_2", new MockComponentDefinition("sink_2", ExecutionEngine.DI_SPARK_BATCH, OUTGOING));
        definitions.put("sink_3", new MockComponentDefinition("sink_3", ExecutionEngine.DI_SPARK_BATCH, OUTGOING));
        definitions.put("transformer_1", new MockComponentDefinition("transformer_1", "icon_key_transformer_1",
                ExecutionEngine.DI_SPARK_STREAMING, INCOMING_AND_OUTGOING));
        definitions.put("transformer_2",
                new MockComponentDefinition("transformer_2", ExecutionEngine.DI_SPARK_STREAMING, INCOMING_AND_OUTGOING));
        definitions.put("transformer_3",
                new MockComponentDefinition("transformer_3", ExecutionEngine.DI_SPARK_STREAMING, INCOMING_AND_OUTGOING));
        definitions.put("config_1", new MockComponentDefinition("config_1", "icon_key_config_1", NONE));
        definitions.put("config_2", new MockComponentDefinition("config_2", NONE));
        definitions.put("config_3", new MockComponentDefinition("config_3", NONE));
        definitions.put("s&s_1", new MockComponentDefinition("ss_1", "icon_key_ss_1", INCOMING, OUTGOING));
        definitions.put("s&s_2", new MockComponentDefinition("ss_2", INCOMING, OUTGOING));
        return definitions;
    }
}
