package org.talend.components.processing.runtime.typeconverter;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties;
import org.talend.daikon.properties.ValidationResult;

public class TypeConverterRuntime extends PTransform<PCollection<IndexedRecord>, PCollection<IndexedRecord>>
        implements RuntimableRuntime<TypeConverterProperties> {

    private TypeConverterProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, TypeConverterProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    public TypeConverterRuntime withProperties(TypeConverterProperties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public PCollection<IndexedRecord> expand(PCollection<IndexedRecord> input) {
        TypeConverterDoFn function = new TypeConverterDoFn().withProperties(properties);
        PCollection<IndexedRecord> output = input.apply("TypeConverter", ParDo.of(function))
                .setCoder(LazyAvroCoder.<IndexedRecord>of());
        return output;
    }

}
