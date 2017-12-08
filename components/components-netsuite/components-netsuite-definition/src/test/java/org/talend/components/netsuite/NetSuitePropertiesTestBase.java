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

package org.talend.components.netsuite;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.talend.components.netsuite.schema.SearchFieldInfo;
import org.talend.components.netsuite.schema.SearchInfo;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.java8.Function;

/**
 *
 */
public abstract class NetSuitePropertiesTestBase {

    public static void installMockRuntimeInvoker(MockRuntimeInvoker runtimeInvoker) {
        NetSuiteComponentDefinition.setRuntimeInvoker(runtimeInvoker);
    }

    public static void installMockRuntime(NetSuiteRuntime runtime) {
        installMockRuntimeInvoker(new MockRuntimeInvoker(runtime));
    }

    protected TestDataset createTestDataset1() {
        TestDataset dataset = new TestDataset();

        dataset.getRecordTypes().addAll(Arrays.<NamedThing>asList(
                new SimpleNamedThing("Account"),
                new SimpleNamedThing("Opportunity")
        ));

        dataset.getSearchableTypes().addAll(Arrays.<NamedThing>asList(
                new SimpleNamedThing("Account"),
                new SimpleNamedThing("Opportunity"),
                new SimpleNamedThing("Transaction")
        ));

        dataset.getTypeSchemaMap().put("Account", SchemaBuilder.builder().record("Account")
                .fields()
                .name("AccountField1").type(AvroUtils._boolean()).noDefault()
                .name("AccountField2").type(AvroUtils._string()).noDefault()
                .name("AccountField3").type(AvroUtils._logicalTimestamp()).noDefault()
                .endRecord()
        );
        dataset.getTypeSchemaMap().put("Opportunity", SchemaBuilder.builder().record("Opportunity")
                .fields()
                .name("OpportunityField1").type(AvroUtils._boolean()).noDefault()
                .name("OpportunityField2").type(AvroUtils._double()).noDefault()
                .name("OpportunityField3").type(AvroUtils._logicalTimestamp()).noDefault()
                .endRecord()
        );
        dataset.getTypeSchemaMap().put("RecordRef", SchemaBuilder.builder().record("RecordRef")
                .fields()
                .name("InternalId").type(AvroUtils._boolean()).noDefault()
                .name("Type").type(AvroUtils._string()).noDefault()
                .endRecord()
        );

        dataset.getSearchOperators().addAll(Arrays.<NamedThing>asList(
                new SimpleNamedThing("Boolean"),
                new SimpleNamedThing("Date.onOrAfter"),
                new SimpleNamedThing("String.contains")
        ));

        dataset.getSearchInfoMap().put("Account",
            new SearchInfo("Account", Arrays.asList(
                    new SearchFieldInfo("AccountField1", Boolean.class),
                    new SearchFieldInfo("AccountField2", String.class),
                    new SearchFieldInfo("AccountField3", XMLGregorianCalendar.class)
            ))
        );
        dataset.getSearchInfoMap().put("Opportunity",
                new SearchInfo("Opportunity", Arrays.asList(
                        new SearchFieldInfo("OpportunityField1", Boolean.class),
                        new SearchFieldInfo("OpportunityField2", Double.class),
                        new SearchFieldInfo("OpportunityField3", XMLGregorianCalendar.class)
                ))
        );

        return dataset;
    }

    public static class MockRuntimeInvoker implements NetSuiteComponentDefinition.RuntimeInvoker {
        protected NetSuiteRuntime runtime;

        public MockRuntimeInvoker(NetSuiteRuntime runtime) {
            this.runtime = runtime;
        }

        @Override
        public <R> R invokeRuntime(NetSuiteRuntime.Context context, NetSuiteProvideConnectionProperties properties,
                Function<NetSuiteRuntime, R> func) {
            runtime.setContext(context);
            return func.apply(runtime);
        }
    }

    public static class DatasetMockTestFixture {
        protected TestDataset testDataset;
        protected NetSuiteRuntime runtime;
        protected NetSuiteDatasetRuntime datasetRuntime;

        public DatasetMockTestFixture(TestDataset testDataset) {
            this.testDataset = testDataset;
        }

        public void setUp() throws Exception {
            runtime = mock(NetSuiteRuntime.class);
            datasetRuntime = mock(NetSuiteDatasetRuntime.class);

            installMockRuntime(runtime);

            // Types

            doReturn(new ArrayList<>(testDataset.getRecordTypes()))
                    .when(datasetRuntime).getRecordTypes();

            when(datasetRuntime.getSchema(anyString())).then(new Answer<Schema>() {
                @Override public Schema answer(InvocationOnMock invocationOnMock) throws Throwable {
                    String typeName = (String) invocationOnMock.getArguments()[0];
                    return testDataset.getTypeSchemaMap().get(typeName);
                }
            });

            when(datasetRuntime.getSchemaForUpdate(anyString())).then(new Answer<Schema>() {
                @Override public Schema answer(InvocationOnMock invocationOnMock) throws Throwable {
                    String typeName = (String) invocationOnMock.getArguments()[0];
                    return testDataset.getTypeSchemaMap().get(typeName);
                }
            });

            when(datasetRuntime.getSchemaForDelete(anyString())).then(new Answer<Schema>() {
                @Override public Schema answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return testDataset.getTypeSchemaMap().get("RecordRef");
                }
            });

            // Search

            doReturn(new ArrayList<>(testDataset.getSearchableTypes()))
                    .when(datasetRuntime).getSearchableTypes();
            doReturn(new ArrayList<>(testDataset.getSearchOperators()))
                    .when(datasetRuntime).getSearchFieldOperators();

            when(datasetRuntime.getSearchInfo(anyString())).then(new Answer<SearchInfo>() {
                @Override public SearchInfo answer(InvocationOnMock invocationOnMock) throws Throwable {
                    String typeName = (String) invocationOnMock.getArguments()[0];
                    return testDataset.getSearchInfoMap().get(typeName);
                }
            });
        }

        public void tearDown() throws Exception {

        }

        public TestDataset getTestDataset() {
            return testDataset;
        }

        public NetSuiteRuntime getRuntime() {
            return runtime;
        }

        public NetSuiteDatasetRuntime getDatasetRuntime() {
            return datasetRuntime;
        }
    }

    public static class TestDataset {
        protected Collection<NamedThing> recordTypes = new HashSet<>();
        protected Collection<NamedThing> searchableTypes = new HashSet<>();
        protected Map<String, Schema> typeSchemaMap = new HashMap<>();
        protected Collection<NamedThing> searchOperators = new HashSet<>();
        protected Map<String, SearchInfo> searchInfoMap = new HashMap<>();

        public Collection<NamedThing> getRecordTypes() {
            return recordTypes;
        }

        public void setRecordTypes(Collection<NamedThing> recordTypes) {
            this.recordTypes = recordTypes;
        }

        public Collection<NamedThing> getSearchableTypes() {
            return searchableTypes;
        }

        public void setSearchableTypes(Collection<NamedThing> searchableTypes) {
            this.searchableTypes = searchableTypes;
        }

        public Map<String, Schema> getTypeSchemaMap() {
            return typeSchemaMap;
        }

        public void setTypeSchemaMap(Map<String, Schema> typeSchemaMap) {
            this.typeSchemaMap = typeSchemaMap;
        }

        public Collection<NamedThing> getSearchOperators() {
            return searchOperators;
        }

        public void setSearchOperators(Collection<NamedThing> searchOperators) {
            this.searchOperators = searchOperators;
        }

        public Map<String, SearchInfo> getSearchInfoMap() {
            return searchInfoMap;
        }

        public void setSearchInfoMap(Map<String, SearchInfo> searchInfoMap) {
            this.searchInfoMap = searchInfoMap;
        }
    }
}
