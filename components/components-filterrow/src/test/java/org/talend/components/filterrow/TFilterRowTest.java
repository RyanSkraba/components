
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
package org.talend.components.filterrow;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.OperatorType;
import org.talend.components.filterrow.processing.LogicalOperator;
import org.talend.components.filterrow.runtime.TFilterRowSink;
import org.talend.daikon.avro.SchemaConstants;

@SuppressWarnings("nls")
public class TFilterRowTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentService componentService;

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test.
    public ComponentService getComponentService() {
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();
            testComponentRegistry.registerComponentFamilyDefinition(new TFilterRowFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void testTFilterRowRuntime() throws Exception {
        TFilterRowProperties props = (TFilterRowProperties) getComponentService().getComponentProperties("tFilterRow_POC");

        props.schemaListener.afterSchema();

        assertThat(props.conditionsTable.isEditable(), is(false));

        Schema schema = SchemaBuilder.builder().record("testRecord").fields().name("field1").type().stringType().noDefault()
                .endRecord();
        props.schemaMain.schema.setValue(schema);

        final List<Schema.Field> additionalRejectFields = new ArrayList<Schema.Field>();

        Schema.Field field = null;
        field = new Schema.Field(TFilterRowProperties.FIELD_ERROR_MESSAGE, Schema.create(Schema.Type.STRING), null,
                (Object) null);
        field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
        field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
        additionalRejectFields.add(field);

        Schema rejectSchema = newSchema(schema, "rejectOutput", additionalRejectFields);

        props.schemaListener.afterSchema();

        assertThat(props.schemaReject.schema.getValue(), is((Schema) rejectSchema));
        assertThat(props.schemaFlow.schema.getValue(), is((Schema) schema));
        assertThat(props.conditionsTable.isEditable(), is(true));

        props.conditionsTable.columnName.setValue(Arrays.asList("field1"));
        props.conditionsTable.function.setValue(Arrays.asList(FunctionType.EMPTY));
        props.conditionsTable.operator.setValue(Arrays.asList(OperatorType.EQUALS));
        props.conditionsTable.value.setValue(Arrays.asList((Object) "test"));

        props.logicalOperator.setValue(LogicalOperator.Or);

        Sink sink = new TFilterRowSink();
        sink.initialize(null, props);

        WriteOperation<?> writeOperation = sink.createWriteOperation();
        writeOperation.initialize(null);
        WriterWithFeedback<?, ?, ?> writer = (WriterWithFeedback<?, ?, ?>) writeOperation.createWriter(null);

        writer.open("uId");
        IndexedRecord record = new GenericData.Record(schema);
        record.put(schema.getField("field1").pos(), "test");
        writer.write(record);
        Iterable<?> success = writer.getSuccessfulWrites();
        Iterator<?> successIterator = success.iterator();
        assertThat(successIterator.hasNext(), is(true));
        IndexedRecord successWrite = (IndexedRecord) successIterator.next();
        assertThat(successWrite.getSchema(), is(schema));
        assertThat(successWrite, is(record));
        writer.cleanWrites();

        record = new GenericData.Record(schema);
        record.put(schema.getField("field1").pos(), "test123");
        writer.write(record);
        success = writer.getSuccessfulWrites();
        successIterator = success.iterator();
        assertThat(successIterator.hasNext(), is(false));
        Iterable<?> reject = writer.getRejectedWrites();
        Iterator<?> rejectIterator = reject.iterator();
        assertThat(rejectIterator.hasNext(), is(true));
        IndexedRecord rejectWrite = (IndexedRecord) rejectIterator.next();
        assertThat(rejectWrite.getSchema(), is(rejectSchema));
        Result result = (Result) writer.close();
        assertThat(result.totalCount, is(2));
        assertThat(result.successCount, is(1));
        assertThat(result.rejectCount, is(1));
    }

    private Schema newSchema(Schema metadataSchema, String newSchemaName, List<Schema.Field> moreFields) {
        Schema newSchema = Schema.createRecord(newSchemaName, metadataSchema.getDoc(), metadataSchema.getNamespace(),
                metadataSchema.isError());
        // TODO duplicate with salesforce, make it to a common one?
        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : metadataSchema.getFields()) {
            Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
            field.getObjectProps().putAll(se.getObjectProps());
            for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }

        copyFieldList.addAll(moreFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String, Object> entry : metadataSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

}
