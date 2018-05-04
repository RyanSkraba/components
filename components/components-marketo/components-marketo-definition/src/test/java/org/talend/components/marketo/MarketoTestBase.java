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
package org.talend.components.marketo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.test.AbstractComponentTest2;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkRuntime;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkSchemaProvider;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.sandbox.SandboxedInstance;

public class MarketoTestBase extends AbstractComponentTest2 {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentService componentService;

    @Inject
    DefinitionRegistry testComponentRegistry;

    protected RuntimeContainer adaptor;

    public MarketoTestBase() {
        adaptor = new DefaultComponentRuntimeContainerImpl();
    }

    @Before
    public void initializeComponentRegistryAndService() {
        componentService = null;
    }

    @Override
    public DefinitionRegistryService getDefinitionRegistry() {
        if (testComponentRegistry == null) {
            testComponentRegistry = new DefinitionRegistry();
            testComponentRegistry.registerComponentFamilyDefinition(new MarketoFamilyDefinition());
        }
        return testComponentRegistry;
    }

    public class SandboxedInstanceTestFixture implements AutoCloseable {

        SandboxedInstance sandboxedInstance;

        public MarketoSourceOrSinkRuntime runtimeSourceOrSink;

        public void setUp() throws Exception {
            MarketoComponentDefinition.SandboxedInstanceProvider sandboxedInstanceProvider = mock(
                    MarketoComponentDefinition.SandboxedInstanceProvider.class);
            MarketoComponentDefinition.setSandboxedInstanceProvider(sandboxedInstanceProvider);
            sandboxedInstance = mock(SandboxedInstance.class);
            when(sandboxedInstanceProvider.getSandboxedInstance(anyString(), anyBoolean())).thenReturn(sandboxedInstance);
            runtimeSourceOrSink = mock(MarketoSourceOrSinkRuntime.class,
                    withSettings().extraInterfaces(MarketoSourceOrSinkSchemaProvider.class));
            doReturn(runtimeSourceOrSink).when(sandboxedInstance).getInstance();
            when(runtimeSourceOrSink.initialize(any(RuntimeContainer.class), any(ComponentProperties.class)))
                    .thenReturn(ValidationResult.OK);
            when(runtimeSourceOrSink.validate(any(RuntimeContainer.class))).thenReturn(ValidationResult.OK);
            when(runtimeSourceOrSink.validateConnection(any(MarketoProvideConnectionProperties.class)))
                    .thenReturn(new ValidationResult(Result.OK));
            //
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getAllLeadFields()).thenReturn(fakeAllLeadFields());
            //
            when(runtimeSourceOrSink.getSchemaNames(any(RuntimeContainer.class))).thenReturn(CO_SCHEMA_NAMES);
            when(runtimeSourceOrSink.getEndpointSchema(any(RuntimeContainer.class), eq("car_c"))).thenReturn(CO_CARC_SCHEMA);
            when(runtimeSourceOrSink.getEndpointSchema(any(RuntimeContainer.class), eq("car_except")))
                    .thenThrow(new IOException("ERROR"));
            //
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getSchemaForCustomObject(eq("car_c")))
                    .thenReturn(CO_CARC_SCHEMA);
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getSchemaForCustomObject(eq("car_null")))
                    .thenReturn(null);
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getSchemaForCustomObject(eq("car_except")))
                    .thenThrow(new IOException("ERROR"));
            //
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getCompoundKeyFields(eq("car_c")))
                    .thenReturn(Arrays.asList("brand", "model"));
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getCompoundKeyFields(eq("car_null"))).thenReturn(null);
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getCompoundKeyFields(eq("car_except")))
                    .thenThrow(new IOException("ERROR"));
            when(((MarketoSourceOrSinkSchemaProvider) runtimeSourceOrSink).getSchemaNames(null)).thenReturn(CO_SCHEMA_NAMES);
        }

        public void changeValidateConnectionResult(Result result) {
            when(runtimeSourceOrSink.validateConnection(any(MarketoProvideConnectionProperties.class)))
                    .thenReturn(new ValidationResult(result));
        }

        public void tearDown() throws Exception {
            MarketoComponentDefinition
                    .setSandboxedInstanceProvider(MarketoComponentDefinition.SandboxedInstanceProvider.INSTANCE);
        }

        @Override
        public void close() throws Exception {
            tearDown();
        }
    }

    public static List<Field> fakeAllLeadFields() {
        List<Field> fields = new ArrayList<>();
        Field field = new Schema.Field("id", Schema.create(Type.INT), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("email", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("accountType", Schema.create(Schema.Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("linkedInId", Schema.create(Type.INT), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("sfdcAccountId", Schema.create(Type.STRING), null, (Object) null);
        fields.add(field);
        field = new Schema.Field("company", Schema.create(Type.STRING), null, (Object) null);
        fields.add(field);

        return fields;
    }

    static final NamedThing ntCar = new SimpleNamedThing("car_c");

    static final NamedThing ntSP = new SimpleNamedThing("smartphone_c");

    public static final List<NamedThing> CO_SCHEMA_NAMES = Arrays.asList(ntCar, ntSP);

    public static final Schema CO_CARC_SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
            .name("brand").type().stringType().noDefault() //
            .name("model").type().stringType().noDefault() //
            .endRecord();

    static class RepoProps {

        Properties props;

        String name;

        String repoLocation;

        Schema schema;

        String schemaPropertyName;

        RepoProps(Properties props, String name, String repoLocation, String schemaPropertyName) {
            this.props = props;
            this.name = name;
            this.repoLocation = repoLocation;
            this.schemaPropertyName = schemaPropertyName;
            if (schemaPropertyName != null) {
                this.schema = (Schema) props.getValuedProperty(schemaPropertyName).getValue();
            }
        }
    }

    class TestRepository implements Repository {

        private int locationNum;

        public String componentIdToCheck;

        public ComponentProperties properties;

        public List<RepoProps> repoProps;

        TestRepository(List<RepoProps> repoProps) {
            this.repoProps = repoProps;
        }

        @Override
        public String storeProperties(Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
            RepoProps rp = new RepoProps(properties, name, repositoryLocation, schemaPropertyName);
            repoProps.add(rp);
            return repositoryLocation + ++locationNum;
        }
    }

    private final List<RepoProps> repoProps = new ArrayList<>();

    protected Repository repo = new TestRepository(repoProps);

}
