package org.talend.components.api.component.runtime;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.util.AvroUtils;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

public class RuntimeHelperTest {

    @Test
    public void testResolveSchema() throws Exception {

        Schema designSchema = SchemaBuilder.record("design").fields()
                .name("f1").type().stringType().noDefault()
                .name("f2").type().stringType().noDefault()
                .name("dynamic").type().bytesType().noDefault()
                .name("f3").type().stringType().noDefault()
                .endRecord();

        AvroUtils.setFieldDynamic(designSchema.getField("dynamic"));

        Schema resolved = RuntimeHelper.resolveSchema(null, new ComponentProperties("schema1") {
            @Override
            protected boolean acceptUninitializedField(Field f) {
                return super.acceptUninitializedField(f);
            }
        }, new SourceOrSink() {
            @Override
            public void initialize(RuntimeContainer adaptor, ComponentProperties properties) {
            }

            @Override
            public ValidationResult validate(RuntimeContainer adaptor) {
                return null;
            }

            @Override
            public List<NamedThing> getSchemaNames(RuntimeContainer adaptor) throws IOException {
                return null;
            }

            @Override
            public Schema getSchema(RuntimeContainer adaptor, ComponentProperties properties) throws IOException {
                Schema runtimeSchema = SchemaBuilder.record("design").fields()
                        .name("f1").type().stringType().noDefault()
                        .name("r1").type().stringType().noDefault()
                        .name("r2").type().stringType().noDefault()
                        .endRecord();

                return runtimeSchema;
            }
        }, designSchema);

        System.out.println(resolved);
        List<Schema.Field> field = resolved.getFields();
        assertEquals("f1", field.get(0).name());
        assertEquals("f2", field.get(1).name());
        assertEquals("r1", field.get(2).name());
        assertEquals("r2", field.get(3).name());
        assertEquals("f3", field.get(4).name());
    }
}