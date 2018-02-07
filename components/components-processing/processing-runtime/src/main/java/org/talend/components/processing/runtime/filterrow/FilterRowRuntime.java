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
package org.talend.components.processing.runtime.filterrow;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.Filter;
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
import org.talend.components.processing.definition.filterrow.FilterRowProperties;
import org.talend.daikon.properties.ValidationResult;

public class FilterRowRuntime implements BeamJobBuilder, RuntimableRuntime<FilterRowProperties> {

    private final static TupleTag<IndexedRecord> flowOutput = new TupleTag<IndexedRecord>() {
    };

    final static TupleTag<IndexedRecord> rejectOutput = new TupleTag<IndexedRecord>() {
    };

    private FilterRowProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, FilterRowProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    @Override
    public void build(BeamJobContext ctx) {
        String mainLink = ctx.getLinkNameByPortName("input_" + properties.MAIN_CONNECTOR.getName());
        if (!StringUtils.isEmpty(mainLink)) {
            PCollection<IndexedRecord> mainPCollection = ctx.getPCollectionByLinkName(mainLink);
            if (mainPCollection != null) {
                String flowLink = ctx.getLinkNameByPortName("output_" + properties.FLOW_CONNECTOR.getName());
                String rejectLink = ctx.getLinkNameByPortName("output_" + properties.REJECT_CONNECTOR.getName());

                boolean hasFlow = !StringUtils.isEmpty(flowLink);
                boolean hasReject = !StringUtils.isEmpty(rejectLink);

                if (hasFlow && hasReject) {
                    // If both of the outputs are present, the DoFn must be used.
                    PCollectionTuple outputTuples = mainPCollection.apply(ctx.getPTransformName(),
                            ParDo.of(new FilterRowDoFn(properties)).withOutputTags(flowOutput, TupleTagList.of(rejectOutput)));
                    ctx.putPCollectionByLinkName(flowLink, outputTuples.get(flowOutput));
                    ctx.putPCollectionByLinkName(rejectLink, outputTuples.get(rejectOutput));
                } else if (hasFlow || hasReject) {
                    // If only one of the outputs is present, the predicate can be used for efficiency.
                    FilterRowPredicate predicate = hasFlow //
                            ? new FilterRowPredicate(properties) //
                            : new FilterRowPredicate.Negate(properties);
                    PCollection<IndexedRecord> output = mainPCollection.apply(ctx.getPTransformName(), Filter.by(predicate));
                    ctx.putPCollectionByLinkName(hasFlow ? flowLink : rejectLink, output);
                } else {
                    // If neither are specified, then don't do anything. This component could have been cut from the pipeline.
                }
            }
        }
    }
}
