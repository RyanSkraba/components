package org.talend.components.processing.runtime.replicate;

import org.apache.beam.sdk.values.PCollection;

import org.talend.components.adapter.beam.BeamJobBuilder;
import org.talend.components.adapter.beam.BeamJobContext;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.replicate.ReplicateProperties;
import org.talend.daikon.properties.ValidationResult;
import org.apache.commons.lang3.StringUtils;

public class ReplicateRuntime implements BeamJobBuilder, RuntimableRuntime<ReplicateProperties> {

    private ReplicateProperties properties;

    private boolean hasFlow;

    private boolean hasSecondFlow;

    @Override
    public void build(BeamJobContext ctx) {
        String mainLink = ctx.getLinkNameByPortName("input_" + properties.MAIN_CONNECTOR.getName());
        if (!StringUtils.isEmpty(mainLink)) {
            PCollection<Object> mainPCollection = ctx.getPCollectionByLinkName(mainLink);
            if (mainPCollection != null) {
                String flowLink = ctx.getLinkNameByPortName("output_" + properties.FLOW_CONNECTOR.getName());
                String secondFlowLink = ctx.getLinkNameByPortName("output_" + properties.SECOND_FLOW_CONNECTOR.getName());

                hasFlow = !StringUtils.isEmpty(flowLink);
                hasSecondFlow = !StringUtils.isEmpty(secondFlowLink);

                if (hasFlow) {
                    ctx.putPCollectionByLinkName(flowLink, mainPCollection);
                }
                if (hasSecondFlow) {
                    ctx.putPCollectionByLinkName(secondFlowLink, mainPCollection);
                }
            }
        }
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ReplicateProperties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }
}
