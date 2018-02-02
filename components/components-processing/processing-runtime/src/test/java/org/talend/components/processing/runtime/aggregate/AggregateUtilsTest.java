package org.talend.components.processing.runtime.aggregate;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.processing.definition.aggregate.AggregateFieldOperationType;
import org.talend.components.processing.definition.aggregate.AggregateOperationProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.TalendRuntimeException;

public class AggregateUtilsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void genField() {
        {
            Schema.Field f1 = new Schema.Field("f1", AvroUtils._string(), "", "");
            AggregateOperationProperties funcProps = new AggregateOperationProperties("");
            funcProps.operation.setValue(AggregateFieldOperationType.COUNT);
            Schema.Field newField = AggregateUtils.genField(f1, funcProps);
            Assert.assertEquals("f1_COUNT", newField.name());
            Assert.assertEquals(AvroUtils._long(), AvroUtils.unwrapIfNullable(newField.schema()));
        }

        {
            Schema.Field f2 = new Schema.Field("f2", AvroUtils._string(), "", "");
            AggregateOperationProperties funcProps = new AggregateOperationProperties("");
            funcProps.operation.setValue(AggregateFieldOperationType.COUNT);
            funcProps.outputFieldPath.setValue("new_f2");
            Schema.Field newField = AggregateUtils.genField(f2, funcProps);
            Assert.assertEquals("new_f2", newField.name());
            Assert.assertEquals(AvroUtils._long(), AvroUtils.unwrapIfNullable(newField.schema()));
        }

        {
            // test custom output path with `new.f3`, it will return `f3` only, and `new` will be the parent element
            Schema.Field f3 = new Schema.Field("f3", AvroUtils._string(), "", "");
            AggregateOperationProperties funcProps = new AggregateOperationProperties("");
            funcProps.operation.setValue(AggregateFieldOperationType.COUNT);
            funcProps.outputFieldPath.setValue("new.f3");
            Schema.Field newField = AggregateUtils.genField(f3, funcProps);
            Assert.assertEquals("f3", newField.name());
            Assert.assertEquals(AvroUtils._long(), AvroUtils.unwrapIfNullable(newField.schema()));
        }
    }

    @Test
    public void genFieldType() {
        Schema schema = AggregateUtils.genFieldType(AvroUtils._int(), AggregateFieldOperationType.LIST);
        Assert.assertEquals(Schema.Type.ARRAY, schema.getType());
        Assert.assertTrue(AvroUtils.isSameType(schema.getElementType(), AvroUtils._int()));

        schema = AggregateUtils.genFieldType(AvroUtils._string(), AggregateFieldOperationType.COUNT);
        Assert.assertTrue(AvroUtils.isSameType(schema, AvroUtils._long()));

        thrown.expect(TalendRuntimeException.class);
        AggregateUtils.genFieldType(AvroUtils._string(), AggregateFieldOperationType.SUM);
        schema = AggregateUtils.genFieldType(AvroUtils._int(), AggregateFieldOperationType.SUM);
        Assert.assertEquals(AvroUtils._long(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._long(), AggregateFieldOperationType.SUM);
        Assert.assertEquals(AvroUtils._long(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._float(), AggregateFieldOperationType.SUM);
        Assert.assertEquals(AvroUtils._double(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._double(), AggregateFieldOperationType.SUM);
        Assert.assertEquals(AvroUtils._double(), schema);

        schema = AggregateUtils.genFieldType(AvroUtils._int(), AggregateFieldOperationType.AVG);
        Assert.assertEquals(AvroUtils._double(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._long(), AggregateFieldOperationType.AVG);
        Assert.assertEquals(AvroUtils._double(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._float(), AggregateFieldOperationType.AVG);
        Assert.assertEquals(AvroUtils._double(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._double(), AggregateFieldOperationType.AVG);
        Assert.assertEquals(AvroUtils._double(), schema);

        schema = AggregateUtils.genFieldType(AvroUtils._int(), AggregateFieldOperationType.MIN);
        Assert.assertEquals(AvroUtils._int(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._long(), AggregateFieldOperationType.MIN);
        Assert.assertEquals(AvroUtils._long(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._float(), AggregateFieldOperationType.MIN);
        Assert.assertEquals(AvroUtils._float(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._double(), AggregateFieldOperationType.MIN);
        Assert.assertEquals(AvroUtils._double(), schema);

        schema = AggregateUtils.genFieldType(AvroUtils._int(), AggregateFieldOperationType.MAX);
        Assert.assertEquals(AvroUtils._int(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._long(), AggregateFieldOperationType.MAX);
        Assert.assertEquals(AvroUtils._long(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._float(), AggregateFieldOperationType.MAX);
        Assert.assertEquals(AvroUtils._float(), schema);
        schema = AggregateUtils.genFieldType(AvroUtils._double(), AggregateFieldOperationType.MAX);
        Assert.assertEquals(AvroUtils._double(), schema);
    }
}
