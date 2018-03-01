// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.marklogic.runtime;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicclose.MarkLogicCloseProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;
import org.talend.components.marklogic.tmarklogicoutput.MarkLogicOutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.ForbiddenUserException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseClientFactory.class)
public class MarkLogicSourceOrSinkTest {

    MarkLogicSourceOrSink sourceOrSink;

    @Before
    public void setUp() {
        sourceOrSink = new MarkLogicSourceOrSink();
    }

    @Test
    public void testInitializeOKInputProperties() {
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProperties");
        inputProperties.init();
        ValidationResult vr = sourceOrSink.initialize(null, inputProperties);
        assertEquals(ValidationResult.Result.OK, vr.getStatus());
    }

    @Test
    public void testInitializeOKOutputProperties() {
        MarkLogicOutputProperties outputProperties = new MarkLogicOutputProperties("outputProperties");
        outputProperties.init();
        ValidationResult vr = sourceOrSink.initialize(null, outputProperties);
        assertEquals(ValidationResult.Result.OK, vr.getStatus());
    }

    @Test
    public void testInitializeWrongProperties() {
        MarkLogicCloseProperties closeProperties = new MarkLogicCloseProperties("closeProperties"); //not io
        closeProperties.init();
        ValidationResult vr = sourceOrSink.initialize(null, closeProperties);
        assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
        assertFalse(vr.getMessage().isEmpty());
    }

    @Test
    public void testEndpointSchema() throws IOException {
        Schema nullSchema = sourceOrSink.getEndpointSchema(null, "someSchema");

        assertNull(nullSchema);
    }

    @Test
    public void testGetSchemaNames() throws IOException {
        List<NamedThing> emptyList = sourceOrSink.getSchemaNames(null);

        assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testGetMarkLogicConnectionProperties() {
        MarkLogicInputProperties expectedInputProperties = new MarkLogicInputProperties("inputProperties");
        expectedInputProperties.init();
        sourceOrSink.initialize(null, expectedInputProperties);
        MarkLogicConnectionProperties actualConnectionProperties = sourceOrSink.getMarkLogicConnectionProperties();

        assertEquals(expectedInputProperties.connection, actualConnectionProperties);
    }

    @Test
    public void testValidateWithReferenceConnection() {
        RuntimeContainer container = mock(RuntimeContainer.class);
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        when(container.getComponentData(anyString(), eq(MarkLogicConnection.CONNECTION))).thenReturn(mockedClient);

        MarkLogicConnectionProperties connectionProperties = new MarkLogicConnectionProperties("connection");
        connectionProperties.init();
        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProperties");
        inputProperties.connection.referencedComponent.setReference(connectionProperties);
        inputProperties.connection.referencedComponent.componentInstanceId
                .setValue(MarkLogicConnectionDefinition.COMPONENT_NAME + "_1");
        inputProperties.init();

        sourceOrSink.initialize(container, inputProperties);

        ValidationResult vr = sourceOrSink.validate(container);

        assertEquals(ValidationResult.Result.OK, vr.getStatus());
    }

    @Test
    public void testValidateCannotConnect() {
        DatabaseClient mockedClient = mock(DatabaseClient.class);
        when(mockedClient.openTransaction()).thenThrow(new ForbiddenUserException("User not allowed"));
        PowerMockito.mockStatic(DatabaseClientFactory.class);
        when(DatabaseClientFactory
                .newClient(anyString(), anyInt(), anyString(), (DatabaseClientFactory.SecurityContext) anyObject()))
                .thenReturn(mockedClient);

        MarkLogicInputProperties inputProperties = new MarkLogicInputProperties("inputProperties");
        sourceOrSink.initialize(null, inputProperties);

        assertEquals(Result.ERROR, sourceOrSink.validate(null).getStatus());
    }

    @Test
    public void testDocContentStringTypeSupported() {
        MarkLogicInputProperties testInputProperties = new MarkLogicInputProperties("inputProperties");
        testInputProperties.init();

        Schema stringSchema = prepareSchema(AvroUtils._string(), null);
        testInputProperties.inputSchema.schema.setValue(stringSchema);

        sourceOrSink.checkDocContentTypeSupported(testInputProperties.datasetProperties.main);
    }

    @Test
    public void testDocContentBytesTypeSupported() {
        MarkLogicInputProperties testInputProperties = new MarkLogicInputProperties("inputProperties");
        testInputProperties.init();

        Schema bytesSchema = prepareSchema(AvroUtils._bytes(), null);
        testInputProperties.inputSchema.schema.setValue(bytesSchema);

        sourceOrSink.checkDocContentTypeSupported(testInputProperties.datasetProperties.main);
    }

    @Test
    public void testDocContentObjectTypeSupported() {
        MarkLogicInputProperties testInputProperties = new MarkLogicInputProperties("inputProperties");
        testInputProperties.init();

        Schema bytesSchema = prepareSchema(AvroUtils._short(), "id_Object");
        testInputProperties.datasetProperties.main.schema.setValue(bytesSchema);

        sourceOrSink.checkDocContentTypeSupported(testInputProperties.datasetProperties.main);
    }

    @Test
    public void testDocContentDocumentTypeSupported() {
        MarkLogicInputProperties testInputProperties = new MarkLogicInputProperties("inputProperties");
        testInputProperties.init();

        Schema bytesSchema = prepareSchema(AvroUtils._short(), "id_Document");
        testInputProperties.datasetProperties.main.schema.setValue(bytesSchema);

        sourceOrSink.checkDocContentTypeSupported(testInputProperties.datasetProperties.main);
    }

    @Test(expected = MarkLogicException.class)
    public void testIntegerTalendTypeNotSupported() {
        MarkLogicInputProperties testInputProperties = new MarkLogicInputProperties("inputProperties");
        testInputProperties.init();

        Schema bytesSchema = prepareSchema(AvroUtils._short(), "id_Integer");
        testInputProperties.datasetProperties.main.schema.setValue(bytesSchema);

        sourceOrSink.checkDocContentTypeSupported(testInputProperties.datasetProperties.main);
    }

    @Test(expected = MarkLogicException.class)
    public void testIntegerAvroTypeNotSupported() {
        MarkLogicInputProperties testInputProperties = new MarkLogicInputProperties("inputProperties");
        testInputProperties.init();

        Schema bytesSchema = prepareSchema(AvroUtils._int(), null);
        testInputProperties.datasetProperties.main.schema.setValue(bytesSchema);

        sourceOrSink.checkDocContentTypeSupported(testInputProperties.datasetProperties.main);
    }

    @Test(expected = MarkLogicException.class)
    public void testDocContentTypeNotExists() {
        MarkLogicInputProperties testInputProperties = new MarkLogicInputProperties("inputProperties");
        testInputProperties.init();

        Schema wrongSchema = AvroUtils.createEmptySchema();
        testInputProperties.datasetProperties.main.schema.setValue(wrongSchema);
        sourceOrSink.checkDocContentTypeSupported(testInputProperties.datasetProperties.main);
    }

    private Schema prepareSchema(Schema docContentType, String diTalendTypeProperty) {

        Schema.Field docIdField = new Schema.Field("docId", AvroUtils._string(), null, (Object) null,
                Schema.Field.Order.ASCENDING);
        Schema.Field docContentField = new Schema.Field("docContent", docContentType, null, (Object) null,
                Schema.Field.Order.IGNORE);
        if (diTalendTypeProperty != null) {
            docContentField.addProp("di.column.talendType", diTalendTypeProperty);
        }
        List<Schema.Field> fields = new ArrayList<>();
        fields.add(docIdField);
        fields.add(docContentField);

        Schema componentSchema = Schema.createRecord("markLogic", null, null, false, fields);

        return componentSchema;
    }
}
