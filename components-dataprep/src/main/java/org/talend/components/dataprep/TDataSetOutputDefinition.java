package org.talend.components.dataprep;

import org.talend.components.api.Constants;
import org.talend.components.api.component.*;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;

import aQute.bnd.annotation.component.Component;

/**
 * The TDataSetOutputDefinition acts as an entry point for all of services that
 * a component provides to integrate with the Studio (at design-time) and other 
 * components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDataSetOutputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDataSetOutputDefinition extends AbstractComponentDefinition implements OutputComponentDefinition {

    public static final String COMPONENT_NAME = "tDataSetOutput"; //$NON-NLS-1$


    public TDataSetOutputDefinition() {
        super(COMPONENT_NAME);
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 1), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Data Preparation" }; //$NON-NLS-1$
    }

    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-dataprep";
    }
    
    
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDataSetOutputProperties.class;
    }

    @Override
    public Sink getRuntime() {
        return new TDataSetOutputSink();
    }
}
