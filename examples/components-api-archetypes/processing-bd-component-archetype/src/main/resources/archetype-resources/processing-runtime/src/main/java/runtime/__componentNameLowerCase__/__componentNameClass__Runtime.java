package ${package}.runtime.${componentNameLowerCase};

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

import org.apache.beam.sdk.values.PCollection;

import ${packageTalend}.adapter.beam.BeamJobBuilder;
import ${packageTalend}.adapter.beam.BeamJobContext;
import ${packageTalend}.api.component.runtime.RuntimableRuntime;
import ${packageTalend}.api.container.RuntimeContainer;
import ${packageTalend}.processing.definition.${componentNameLowerCase}.${componentNameClass}Properties;
import ${packageDaikon}.properties.ValidationResult;
import org.apache.commons.lang3.StringUtils;

public class ${componentNameClass}Runtime implements RuntimableRuntime<${componentNameClass}Properties> {

    private ${componentNameClass}Properties properties;

    private boolean hasFlow;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ${componentNameClass}Properties componentProperties) {
        this.properties = componentProperties;
        return ValidationResult.OK;
    }

    /*

    **
    *   You need to extends DoFn
    *   Example : extends DoFn<Object, Object>
    **

    @ProcessElement
    public void processElement(ProcessContext context) throws IOException {
        if (context.element() != null) {
            context.output(context.element());
        }
    }


     */

    /*
    **
    * ### You need to extends PTransform
    * Example : extends PTransform<PCollection<IndexedRecord>, PCollection<IndexedRecord>>
    **

    @Override
    public PCollection<IndexedRecord> expand(PCollection<IndexedRecord> input) {
        ${componentNameClass}DoFn function = new ${componentNameClass}DoFn().withProperties(properties);
        PCollection<IndexedRecord> output = input.apply("${componentNameClass}", ParDo.of(function))
                .setCoder(LazyAvroCoder.<IndexedRecord>of());
        return output;
    }
    */

    /*
    **
    * ### You need to implements the class "BeamJobBuilder"
    **

    @Override
    public void build(BeamJobContext ctx) {

        *
        * Example of runtime action for a pass-through behaviour component

        String mainLink = ctx.getLinkNameByPortName("input_" + properties.INPUT_CONNECTOR.getName());
        if (!StringUtils.isEmpty(mainLink)) {
            PCollection<Object> mainPCollection = ctx.getPCollectionByLinkName(mainLink);
            if (mainPCollection != null) {
                String flowLink = ctx.getLinkNameByPortName("output_" + properties.OUTPUT_CONNECTOR.getName());
                hasFlow = !StringUtils.isEmpty(flowLink);
                if (hasFlow) {
                    ctx.putPCollectionByLinkName(flowLink, mainPCollection);
                }
            }
        }
    }
    */
}
