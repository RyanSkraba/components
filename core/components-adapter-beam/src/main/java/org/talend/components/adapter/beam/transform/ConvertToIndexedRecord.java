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

package org.talend.components.adapter.beam.transform;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * Transformation to turn a {@link PCollection} of any type into a {@link PCollection} of {@link IndexedRecord}s.
 *
 * This class implements some standard logic about the conversions present in the {@link AvroRegistry}, which can be
 * configured to "know" how to interpret technology-specific types (in the current virtual machine).
 *
 * @param <DatumT> The type of the incoming collection.
 * @param <AvroT> If more type information is known about the expected output, it can be included. Otherwise, this
 * should be just an IndexedRecord.
 */
public class ConvertToIndexedRecord<DatumT, AvroT extends IndexedRecord> extends
        PTransform<PCollection<DatumT>, PCollection<AvroT>> {

    /** Use the {@link #of()} method to create. */
    protected ConvertToIndexedRecord() {
    }

    /**
     * @return an instance of this transformation.
     */
    public static <DatumT, AvroT extends IndexedRecord> ConvertToIndexedRecord<DatumT, AvroT> of() {
        return new ConvertToIndexedRecord<DatumT, AvroT>();
    }

    /**
     * Converts any datum to an {@link IndexedRecord} representation as if it were passed in the transformation. This
     * might be an expensive call, so it should only be used for sampling data (not in a processing-intensive loop).
     * 
     * @param datum the datum to convert.
     * @return its representation as an Avro {@link IndexedRecord}.
     */
    public static <DatumT, AvroT extends IndexedRecord> AvroT convertToAvro(DatumT datum) {
        IndexedRecordConverter c = new AvroRegistry().createIndexedRecordConverter(datum.getClass());
        if (c == null) {
            throw new Pipeline.PipelineExecutionException(new RuntimeException("Cannot convert " + datum.getClass()
                    + " to IndexedRecord."));
        }
        return (AvroT) c.convertToAvro(datum);
    }

    @Override
    public PCollection<AvroT> apply(PCollection<DatumT> input) {
        return input.apply(ParDo.of(new DoFn<DatumT, AvroT>() {

            /** The converter is cached for performance. */
            private transient IndexedRecordConverter<? super DatumT, ?> converter;

            @DoFn.ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                DatumT in = c.element();
                if (in == null) {
                    return;
                }
                if (converter == null) {
                    converter = (IndexedRecordConverter<? super DatumT, ? extends IndexedRecord>) new AvroRegistry()
                            .createIndexedRecordConverter(in.getClass());
                }
                c.output((AvroT) converter.convertToAvro(in));
            }

        }));
    }

}
