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
package org.talend.components.processing.runtime.normalize;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.normalize.NormalizeProperties;
import org.talend.daikon.properties.ValidationResult;

public class NormalizeRuntime extends PTransform<PCollection<IndexedRecord>, PCollection>
        implements RuntimableRuntime<NormalizeProperties> {

    private NormalizeProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, NormalizeProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection expand(PCollection<IndexedRecord> inputPCollection) {
        NormalizeDoFn doFn = new NormalizeDoFn() //
                .withProperties(properties);

        PCollection outputCollection = inputPCollection.apply(ParDo.of(doFn));
        return outputCollection;
    }
}
