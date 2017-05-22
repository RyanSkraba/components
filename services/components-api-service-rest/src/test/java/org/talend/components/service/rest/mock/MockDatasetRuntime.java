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

package org.talend.components.service.rest.mock;

import static org.apache.avro.SchemaBuilder.builder;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

public class MockDatasetRuntime implements DatasetRuntime<MockDatasetProperties> {

    // Build a default schema as in the builder javadoc example
    private static final Schema schema = builder().record("myMockRecord") //
            .namespace(MockDatasetRuntime.class.getName()) //
            .fields() //
            .name("clientHash").type().stringType().noDefault() //
            .name("clientProtocol").type().stringType().noDefault() //
            .name("serverHash").type().stringType().noDefault() //
            .name("meta").type().nullable().map().values().stringType().noDefault() //
            .endRecord();

    private static final String schemaAsString = "{\"type\":\"record\",\"name\":\"myMockRecord\",\"namespace\":\"org.talend.components.service.rest.mock.MockDatasetRuntime\",\"fields\":[{\"name\":\"clientHash\",\"type\":\"string\"},{\"name\":\"clientProtocol\",\"type\":\"string\"},{\"name\":\"serverHash\",\"type\":\"string\"},{\"name\":\"meta\",\"type\":[{\"type\":\"map\",\"values\":\"string\"},\"null\"]}]}";

    private static final GenericData.Record record = new GenericRecordBuilder(schema) //
            .set("clientHash", "0123456789012345")
            .set("clientProtocol", "http")
            .set("serverHash", "0123456789012345")
            .set("meta", Collections.singletonMap("toto", "dfdfhgoi"))
            .build();

    private static final String recordAsString = "{\"clientHash\":\"0123456789012345\",\"clientProtocol\":\"http\",\"serverHash\":\"0123456789012345\",\"meta\":{\"map\":{\"toto\":\"dfdfhgoi\"}}}";

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        consumer.accept(getSampleRecord());
    }

    public static IndexedRecord getSampleRecord() {
        return record;
    }

    public static String getRecordJsonRepresentation() {
        return recordAsString;
    }

    public static String getSchemaJsonRepresentation() {
        return schemaAsString;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, MockDatasetProperties properties) {
        return new ValidationResult(ValidationResult.Result.OK, "initialized");
    }
}

