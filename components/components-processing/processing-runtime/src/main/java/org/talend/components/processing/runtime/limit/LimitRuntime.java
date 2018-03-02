// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.processing.runtime.limit;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.limit.LimitProperties;
import org.talend.daikon.properties.ValidationResult;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Limit {@link PTransform} used to limit the number of records processed from the source
 * {@link org.apache.beam.sdk.values.PCollection}. At the time of writing, the underlying
 * {@link org.apache.beam.sdk.transforms.DoFn} uses a static counter defined here, which makes it unusable in a
 * distributed mode. Consequently, it can be used only with local runners.
 */
public class LimitRuntime extends PTransform<PCollection<IndexedRecord>, PCollection>
        implements RuntimableRuntime<LimitProperties> {

    public static LimitRuntime of() {
        return new LimitRuntime();
    }

    private LimitProperties properties;

    public static AtomicLong counter = new AtomicLong(0L);

    @Override
    public ValidationResult initialize(RuntimeContainer container, LimitProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection expand(PCollection<IndexedRecord> inputPCollection) {
        LimitDoFn doFn = new LimitDoFn().withProperties(properties);
        return inputPCollection.apply(ParDo.of(doFn));
    }
}
