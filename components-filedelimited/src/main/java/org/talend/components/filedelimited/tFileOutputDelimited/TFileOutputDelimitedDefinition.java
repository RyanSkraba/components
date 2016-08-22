package org.talend.components.filedelimited.tFileOutputDelimited;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.filedelimited.FileDelimitedDefinition;
import org.talend.components.filedelimited.runtime.FileDelimitedSink;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TFileOutputDelimitedDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TFileOutputDelimitedDefinition extends FileDelimitedDefinition implements OutputComponentDefinition {

    public static final String COMPONENT_NAME = "tFileOutputDelimited"; //$NON-NLS-1$

    public TFileOutputDelimitedDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Output" }; //$NON-NLS-1$
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TFileOutputDelimitedProperties.class;
    }

    @Override
    public Sink getRuntime() {
        return new FileDelimitedSink();
    }

}
