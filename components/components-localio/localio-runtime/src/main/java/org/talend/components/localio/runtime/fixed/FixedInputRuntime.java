// ============================================================================
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
// ============================================================================
package org.talend.components.localio.runtime.fixed;

import java.util.LinkedList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.talend.components.adapter.beam.io.rowgenerator.RowGeneratorIO;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.localio.fixed.FixedInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class FixedInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<FixedInputProperties> {

    private FixedInputProperties properties;

    public ValidationResult initialize(RuntimeContainer container, FixedInputProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin begin) {
        FixedDatasetRuntime runtime = new FixedDatasetRuntime();
        runtime.initialize(null, properties.getDatasetProperties());

        // The values to include in the PCollection
        List<IndexedRecord> values = new LinkedList<>();

        if (properties.overrideValuesAction.getValue() == FixedInputProperties.OverrideValuesAction.NONE
                || properties.overrideValuesAction.getValue() == FixedInputProperties.OverrideValuesAction.APPEND) {
            if (!properties.getDatasetProperties().values.getValue().trim().isEmpty()) {
                values.addAll(runtime.getValues(Integer.MAX_VALUE));
            }
        }

        if (properties.overrideValuesAction.getValue() == FixedInputProperties.OverrideValuesAction.APPEND
                || properties.overrideValuesAction.getValue() == FixedInputProperties.OverrideValuesAction.REPLACE) {
            properties.getDatasetProperties().values.setValue(properties.overrideValues.getValue());
            if (!properties.getDatasetProperties().values.getValue().trim().isEmpty()) {
                values.addAll(runtime.getValues(Integer.MAX_VALUE));
            }
        }

        if (values.size() != 0) {
            PCollection<IndexedRecord> out = (PCollection<IndexedRecord>) begin
                    .apply(Create.of(values).withCoder((AvroCoder) AvroCoder.of(runtime.getSchema())));
            if (properties.repeat.getValue() > 1) {
                PCollectionList<IndexedRecord> merged = PCollectionList.of(out);
                for (int i = 2; i < properties.repeat.getValue(); i++)
                    merged = merged.and(out);
                out = merged.apply(Flatten.<IndexedRecord> pCollections());
            }
            return out;
        } else {
            return begin.apply(RowGeneratorIO.read().withSchema(runtime.getSchema()) //
                    .withSeed(0L) //
                    .withPartitions(1) //
                    .withRows(properties.repeat.getValue()));
        }
    }
}
