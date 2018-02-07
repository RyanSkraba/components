package org.talend.components.processing.runtime.filterrow;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.util.Utf8;
import org.junit.Test;
import org.talend.components.processing.definition.filterrow.FilterRowCriteriaProperties;
import org.talend.components.processing.definition.filterrow.FilterRowProperties;

public class FilterRowPredicateTest {

    @Test
    public void test_FilterSimple_utf8() throws Exception {
        FilterRowProperties properties = new FilterRowProperties("test");
        properties.init();
        FilterRowCriteriaProperties filterProp = new FilterRowCriteriaProperties("filter");
        filterProp.init();
        properties.schemaListener.afterSchema();
        filterProp.columnName.setValue("a");
        filterProp.value.setValue("aaa");
        properties.filters.addRow(filterProp);

        Schema inputSimpleSchema = SchemaBuilder.record("inputRow") //
                .fields() //
                .name("a").type().optional().stringType() //
                .name("b").type().optional().stringType() //
                .name("c").type().optional().stringType() //
                .endRecord();

        GenericRecord inputSimpleRecord = new GenericRecordBuilder(inputSimpleSchema) //
                .set("a", new Utf8("aaa")) //
                .set("b", new Utf8("BBB")) //
                .set("c", new Utf8("Ccc")) //
                .build();

        FilterRowPredicate predicate = new FilterRowPredicate(properties);
        assertThat(predicate.apply(inputSimpleRecord), is(Boolean.TRUE));

        predicate = new FilterRowPredicate.Negate(properties);
        assertThat(predicate.apply(inputSimpleRecord), is(Boolean.FALSE));
    }
}