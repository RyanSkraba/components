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

package org.talend.components.salesforce;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.test.TestFixture;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.components.salesforce.schema.SalesforceSchemaHelper;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 *
 */
public class SalesforceTestBase {

    public static final Schema DEFAULT_TEST_SCHEMA_1 = SchemaBuilder.builder().record("Account").fields() //
            .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .endRecord();

    public static final Schema DEFAULT_TEST_SCHEMA_2 = SchemaBuilder.builder().record("Customer").fields() //
            .name("Id").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type().stringType().noDefault() //
            .name("FirstName").type().stringType().noDefault() //
            .name("LastName").type().stringType().noDefault() //
            .endRecord();

    public static TestDataset createDefaultTestDataset() {
        return new TestDataset()
                .module("Account", DEFAULT_TEST_SCHEMA_1)
                .module("Customer", DEFAULT_TEST_SCHEMA_2);
    }

    /**
     * Test fixture which creates mocked {@link SalesforceRuntimeSourceOrSink} which implements
     * {@link SalesforceSchemaHelper}.
     *
     * <p>This class supports {@link AutoCloseable} and can be used in {@code try-with-resources} block.
     */
    public static class MockRuntimeSourceOrSinkTestFixture implements TestFixture, AutoCloseable {

        /**
         * Matcher to be used for matching of properties passed to
         * {@link org.talend.components.api.component.runtime.SourceOrSink#initialize(RuntimeContainer, Properties)} method
         * invoked on mock.
         */
        protected Matcher<? extends ComponentProperties> propertiesMatcher;

        protected TestDataset testDataset;

        public SandboxedInstance sandboxedInstance;

        public SalesforceRuntimeSourceOrSink runtimeSourceOrSink;

        public MockRuntimeSourceOrSinkTestFixture(ComponentProperties properties, TestDataset testDataset) {
            this(equalTo(properties), testDataset);
        }

        public MockRuntimeSourceOrSinkTestFixture(Matcher<? extends ComponentProperties> propertiesMatcher, TestDataset testDataset) {
            this.propertiesMatcher = propertiesMatcher;
            this.testDataset = testDataset;
        }

        public TestDataset getTestDataset() {
            return testDataset;
        }

        public void setTestDataset(TestDataset testDataset) {
            this.testDataset = testDataset;
        }

        @Override
        public void setUp() throws Exception {
            SalesforceDefinition.SandboxedInstanceProvider sandboxedInstanceProvider = mock(
                    SalesforceDefinition.SandboxedInstanceProvider.class);
            SalesforceDefinition.setSandboxedInstanceProvider(sandboxedInstanceProvider);

            sandboxedInstance = mock(SandboxedInstance.class);

            when(sandboxedInstanceProvider.getSandboxedInstance(anyString(), anyBoolean()))
                    .thenReturn(sandboxedInstance);

            runtimeSourceOrSink = mock(SalesforceRuntimeSourceOrSink.class,
                    withSettings().extraInterfaces(SalesforceSchemaHelper.class));

            doReturn(runtimeSourceOrSink).when(sandboxedInstance).getInstance();

            when(runtimeSourceOrSink.initialize(any(), argThat(
                    componentProperties -> propertiesMatcher.matches(componentProperties))))
                    .thenReturn(ValidationResult.OK);

            when(runtimeSourceOrSink.validate(any()))
                    .thenReturn(ValidationResult.OK);

            List<NamedThing> moduleNames = testDataset.getModuleNamesAsNamedThings();

            when(runtimeSourceOrSink.getSchemaNames(any()))
                    .thenReturn(moduleNames);

            when(runtimeSourceOrSink.getEndpointSchema(any(), anyString()))
                    .thenAnswer(new Answer<Schema>() {

                        @Override
                        public Schema answer(InvocationOnMock invocation) throws Throwable {
                            String moduleName = (String) invocation.getArguments()[1];
                            return testDataset.getSchema(moduleName);
                        }
                    });
        }

        @Override
        public void tearDown() throws Exception {
            SalesforceDefinition.setSandboxedInstanceProvider(
                    SalesforceDefinition.SandboxedInstanceProvider.INSTANCE);
        }

        @Override
        public void close() throws Exception {
            tearDown();
        }
    }

    public static class TestDataset {
        protected Set<String> modules = new HashSet<>();
        protected Map<String, Schema> schemas = new HashMap<>();

        public TestDataset() {
        }

        public TestDataset(Map<String, Schema> schemas) {
            this.modules.addAll(schemas.keySet());
            this.schemas.putAll(schemas);
        }

        public TestDataset module(String moduleName) {
            modules.add(moduleName);
            return this;
        }

        public TestDataset module(String moduleName, Schema schema) {
            return module(moduleName).schema(moduleName, schema);
        }

        public TestDataset schema(String moduleName, Schema schema) {
            schemas.put(moduleName, schema);
            return this;
        }

        public Map<String, Schema> getSchemas() {
            return schemas;
        }

        public List<String> getModuleNames() {
            return new ArrayList<>(modules);
        }

        public List<NamedThing> getModuleNamesAsNamedThings() {
            List<NamedThing> namedThingList = new ArrayList<>();
            for (String name : schemas.keySet()) {
                namedThingList.add(new SimpleNamedThing(name));
            }
            return namedThingList;
        }

        public Schema getSchema(String moduleName) {
            return schemas.get(moduleName);
        }
    }

    static class TestRepository implements Repository {

        private int locationNum;

        private String componentIdToCheck;

        private ComponentProperties properties;

        private List<Entry> repoEntries;

        TestRepository(List<Entry> repoEntries) {
            this.repoEntries = repoEntries;
        }

        public String getComponentIdToCheck() {
            return componentIdToCheck;
        }

        public void setComponentIdToCheck(String componentIdToCheck) {
            this.componentIdToCheck = componentIdToCheck;
        }

        public void setProperties(ComponentProperties properties) {
            this.properties = properties;
        }

        public void setRepoEntries(List<Entry> repoEntries) {
            this.repoEntries = repoEntries;
        }

        public ComponentProperties getProperties() {
            return properties;
        }

        public List<Entry> getRepoEntries() {
            return repoEntries;
        }

        @Override
        public String storeProperties(Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
            Entry entry = new Entry(properties, name, repositoryLocation, schemaPropertyName);
            repoEntries.add(entry);
            return repositoryLocation + ++locationNum;
        }

        static class Entry {

            private Properties properties;

            private String name;

            private String repoLocation;

            private Schema schema;

            private String schemaPropertyName;

            Entry(Properties properties, String name, String repoLocation, String schemaPropertyName) {
                this.properties = properties;
                this.name = name;
                this.repoLocation = repoLocation;
                this.schemaPropertyName = schemaPropertyName;
                if (schemaPropertyName != null) {
                    this.schema = (Schema) properties.getValuedProperty(schemaPropertyName).getValue();
                }
            }

            public Properties getProperties() {
                return properties;
            }

            public String getName() {
                return name;
            }

            public String getRepoLocation() {
                return repoLocation;
            }

            public Schema getSchema() {
                return schema;
            }

            public String getSchemaPropertyName() {
                return schemaPropertyName;
            }

            @Override
            public String toString() {
                return "Entry: " + repoLocation + "/" + name + " properties: " + properties;
            }
        }
    }
}
