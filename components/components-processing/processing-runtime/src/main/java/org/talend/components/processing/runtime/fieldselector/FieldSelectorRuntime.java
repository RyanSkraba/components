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
package org.talend.components.processing.runtime.fieldselector;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.adapter.beam.BeamJobBuilder;
import org.talend.components.adapter.beam.BeamJobContext;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.fieldselector.FieldSelectorProperties;
import org.talend.components.processing.definition.filterrow.FilterRowProperties;
import org.talend.components.processing.definition.normalize.NormalizeProperties;
import org.talend.components.processing.runtime.normalize.NormalizeDoFn;
import org.talend.daikon.properties.ValidationResult;

public class FieldSelectorRuntime extends PTransform<PCollection<IndexedRecord>, PCollection>
        implements RuntimableRuntime<FieldSelectorProperties> {

    private FieldSelectorProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, FieldSelectorProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection expand(PCollection<IndexedRecord> inputPCollection) {
        FieldSelectorDoFn doFn = new FieldSelectorDoFn().withProperties(properties);

        return inputPCollection.apply(ParDo.of(doFn));
    }

}
