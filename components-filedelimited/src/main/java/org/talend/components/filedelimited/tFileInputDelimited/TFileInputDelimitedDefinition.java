package org.talend.components.filedelimited.tFileInputDelimited;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.filedelimited.FileDelimitedDefinition;
import org.talend.components.filedelimited.runtime.FileDelimitedSource;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TFileInputDelimitedDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TFileInputDelimitedDefinition extends FileDelimitedDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "tFileInputDelimited"; //$NON-NLS-1$

    public TFileInputDelimitedDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Input" }; //$NON-NLS-1$
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TFileInputDelimitedProperties.class;
    }

    @Override
    public Source getRuntime() {
        return new FileDelimitedSource();
    }

}
