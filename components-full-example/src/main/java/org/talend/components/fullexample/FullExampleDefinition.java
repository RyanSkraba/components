
package org.talend.components.fullexample;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;

import aQute.bnd.annotation.component.Component;

/**
 * The FullExampleDefinition acts as an entry point for all of services that
 * a component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + FullExampleDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class FullExampleDefinition extends AbstractComponentDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "FullExample"; //$NON-NLS-1$

    public FullExampleDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Examples" }; //$NON-NLS-1$
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return "fullExample_icon32.png"; //$NON-NLS-1$
        default:
            return "fullExample_icon32.png"; //$NON-NLS-1$
        }
    }

    @Override
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-full-example";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return FullExampleProperties.class;
    }

    @Override
    public Source getRuntime() {
        return null;
    }
}
