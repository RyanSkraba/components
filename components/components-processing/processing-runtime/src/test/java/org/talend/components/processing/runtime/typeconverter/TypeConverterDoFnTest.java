package org.talend.components.processing.runtime.typeconverter;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties;

import java.math.BigDecimal;
import java.util.List;

public class TypeConverterDoFnTest {

    @Test
    public void testDoFn() throws Exception {
        Schema inputSchema1 = SchemaBuilder.record("inputSchema1") //
                .fields() //
                .name("stringToInt").type().optional().stringType() //
                .name("stringToBool").type().optional().booleanType() //
                .name("stringToDate").type().optional().intType()
                .name("stringToDateWithFormat").type().optional().intType()
                .name("stringToDecimal").type().optional().stringType() //
                //.name("b").type(SchemaBuilder.record("nestedSchema").fields().name("b1").type().bytesType().noDefault().endRecord()).noDefault()
                .endRecord();

        GenericRecordBuilder recordBuilder1 = new GenericRecordBuilder(inputSchema1);
        recordBuilder1.set("stringToInt", "1");
        recordBuilder1.set("stringToBool", "false");
        recordBuilder1.set("stringToDate", "2007-05-20");
        recordBuilder1.set("stringToDateWithFormat", "20/05/2011");
        recordBuilder1.set("stringToDecimal", "1,2345");
        TypeConverterProperties properties = new TypeConverterProperties("test");

        TypeConverterProperties.TypeConverterPropertiesInner converter1 = new TypeConverterProperties.TypeConverterPropertiesInner("converter1");
        converter1.init();
        converter1.field.setValue("stringToInt");
        converter1.outputType.setValue(TypeConverterProperties.TypeConverterOutputTypes.Integer.toString());
        properties.converters.addRow(converter1);

        TypeConverterProperties.TypeConverterPropertiesInner converter2 = new TypeConverterProperties.TypeConverterPropertiesInner("converter2");
        converter2.init();
        converter2.field.setValue("stringToBool");
        converter2.outputType.setValue(TypeConverterProperties.TypeConverterOutputTypes.Boolean.toString());
        properties.converters.addRow(converter2);

        TypeConverterProperties.TypeConverterPropertiesInner converter3 = new TypeConverterProperties.TypeConverterPropertiesInner("converter3");
        converter3.init();
        converter3.field.setValue("stringToDate");
        converter3.outputType.setValue(TypeConverterProperties.TypeConverterOutputTypes.Date.toString());
        properties.converters.addRow(converter3);

        TypeConverterProperties.TypeConverterPropertiesInner converter4 = new TypeConverterProperties.TypeConverterPropertiesInner("converter4");
        converter4.init();
        converter4.field.setValue("stringToDateWithFormat");
        converter4.outputType.setValue(TypeConverterProperties.TypeConverterOutputTypes.Date.toString());
        converter4.outputFormat.setValue("dd/MM/yyyy");
        properties.converters.addRow(converter4);

        TypeConverterProperties.TypeConverterPropertiesInner converter5 = new TypeConverterProperties.TypeConverterPropertiesInner("converter5");
        converter5.init();
        converter5.field.setValue("stringToDecimal");
        converter5.outputType.setValue(TypeConverterProperties.TypeConverterOutputTypes.Decimal.toString());
        converter5.outputFormat.setValue("#,###");
        properties.converters.addRow(converter5);

        //        testConvertValue("1,234", TypeConverterProperties.TypeConverterOutputTypes.Decimal, "#,###", BigDecimal.class);

        TypeConverterDoFn runtime = new TypeConverterDoFn().withProperties(properties);
        DoFnTester<IndexedRecord, IndexedRecord> fnTester = DoFnTester.of(runtime);

        List<IndexedRecord> outputs = fnTester.processBundle(recordBuilder1.build());

        Assert.assertEquals(Integer.class, outputs.get(0).get(0).getClass());
        Assert.assertEquals(Boolean.class, outputs.get(0).get(1).getClass());
        Assert.assertEquals(Long.class, outputs.get(0).get(2).getClass());
        Assert.assertEquals(Long.class, outputs.get(0).get(3).getClass());
        Assert.assertEquals(BigDecimal.class, outputs.get(0).get(4).getClass());
        Assert.assertFalse((Boolean) outputs.get(0).get(1));
        Assert.assertEquals(12345, ((BigDecimal)outputs.get(0).get(4)).intValue());
    }
}
