package org.talend.components.localio.runtime.fixed;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.talend.components.localio.runtime.fixed.FixedInputRuntimeTest.createComponentProperties;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.components.localio.fixed.FixedDatasetProperties;
import org.talend.daikon.avro.SampleSchemas;
import org.talend.daikon.java8.Consumer;

public class FixedDatasetRuntimeTest {

    @Test
    public void testBasic() {
        // The two records to use as values.
        GenericRecord r1 = new GenericData.Record(SampleSchemas.recordSimple());
        r1.put("id", 1);
        r1.put("name", "one");
        GenericRecord r2 = new GenericData.Record(SampleSchemas.recordSimple());
        r2.put("id", 2);
        r2.put("name", "two");

        final FixedDatasetProperties props = createComponentProperties().getDatasetProperties();
        props.format.setValue(FixedDatasetProperties.RecordFormat.AVRO);
        props.schema.setValue(SampleSchemas.recordSimple().toString());
        props.values.setValue(r1.toString() + r2.toString());


        FixedDatasetRuntime runtime = new FixedDatasetRuntime();
        runtime.initialize(null, props);

        // Get the two records.
        final List<IndexedRecord> consumed = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord ir) {
                consumed.add(ir);
            }
        });

        assertThat(consumed, hasSize(2));
    }
}