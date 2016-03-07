package org.talend.components.salesforce.runtime;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.talend.daikon.avro.SchemaConstants.*;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.daikon.avro.util.AvroUtils;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * Unit tests for the {@link SalesforceAvroRegistry}.
 */
@SuppressWarnings("nls")
public class SalesforceAvroRegistryTest {

    private static final SalesforceAvroRegistry sRegistry = SalesforceAvroRegistry.get();

    /**
     * Tests that the {@link SalesforceAvroRegistry} has added support to get a {@link Schema} that describes
     * {@link DescribeSObjectResult} objects.
     */
    @Test
    public void testInferSchemaDescribeSObjectResult() {
        Schema s;
        // Setup.
        {
            Field booleanField = new Field();
            booleanField.setName("valid");
            booleanField.setType(FieldType._boolean);

            DescribeSObjectResult dsor = new DescribeSObjectResult();
            dsor.setName("MySObjectRecord");
            dsor.setFields(new Field[] { booleanField });
            s = sRegistry.inferSchema(dsor);
        }

        assertThat(s.getType(), is(Schema.Type.RECORD));
        assertThat(s.getName(), is("MySObjectRecord"));
        assertThat(s.getFields(), hasSize(1));
        assertThat(s.getObjectProps().keySet(), empty());

        // Check out the field.
        Schema.Field f = s.getFields().get(0);
        assertThat(f.name(), is("valid"));
        assertThat(f.schema().getType(), is(Schema.Type.BOOLEAN));
        assertThat(f.schema().getObjectProps().keySet(), empty());
    }

    /**
     * Tests that the {@link SalesforceAvroRegistry} has added support to get a {@link Schema} that describes
     * {@link Field} objects.
     */
    @Test
    public void testInferSchemaField() {
        // Test boolean extensively.
        Field f = new Field();
        f.setType(FieldType._boolean);

        Schema s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.BOOLEAN));
        // The properties injected into boolean.
        assertThat(s.getObjectProps().keySet(), empty());

        // The same thing if nullable.
        f.setNillable(true);
        s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.UNION));
        assertThat(s.getTypes(), hasSize(2));
        assertThat(s.getObjectProps().keySet(), empty());
        s = AvroUtils.unwrapIfNullable(s);
        assertThat(s.getType(), is(Schema.Type.BOOLEAN));
        assertThat(s.getObjectProps().keySet(), empty());

        // The same thing if a default value.
        f.setDefaultValueFormula(Boolean.TRUE.toString());
        s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.UNION));
        assertThat(s.getTypes(), hasSize(2));
        assertThat(s.getObjectProps().keySet(), empty());
        s = AvroUtils.unwrapIfNullable(s);
        assertThat(s.getType(), is(Schema.Type.BOOLEAN));
        assertThat(s.getObjectProps().keySet(), containsInAnyOrder(TALEND_COLUMN_DEFAULT));
        assertThat(s.getProp(TALEND_COLUMN_DEFAULT), is(Boolean.TRUE.toString()));

        f = new Field();
        f.setType(FieldType._int);
        s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.INT));

        f.setType(FieldType.date);
        s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.LONG));

        f.setType(FieldType.datetime);
        s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.LONG));

        f.setType(FieldType._double);
        s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.DOUBLE));

        f.setType(FieldType.currency);
        f.setPrecision(8);
        f.setScale(5);
        s = sRegistry.inferSchema(f);
        assertThat(s.getType(), is(Schema.Type.BYTES));
        assertThat(s.getLogicalType(), is((LogicalType) LogicalTypes.decimal(8, 5)));
    }
}
