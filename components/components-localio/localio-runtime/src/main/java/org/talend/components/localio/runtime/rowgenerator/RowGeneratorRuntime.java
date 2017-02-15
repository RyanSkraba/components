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
package org.talend.components.localio.runtime.rowgenerator;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.adapter.beam.io.rowgenerator.RowGeneratorIO;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.localio.rowgenerator.RowGeneratorProperties;
import org.talend.daikon.properties.ValidationResult;

public class RowGeneratorRuntime extends PTransform<PBegin, PCollection<IndexedRecord>> implements
        RuntimableRuntime<RowGeneratorProperties> {

    private RowGeneratorProperties properties;

    public ValidationResult initialize(RuntimeContainer container, RowGeneratorProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin begin) {
        RowGeneratorIO.Read read = RowGeneratorIO.read();
        read = read.withSchema(properties.schemaFlow.schema.getValue()) //
                .withPartitions(properties.nbPartitions.getValue()) //
                .withRows(properties.nbRows.getValue());

        if (properties.useSeed.getValue()) {
            read = read.withSeed(properties.seed.getValue());
        }

        // TODO(rskraba): partition skew

        return begin.apply(read);
    }
}
