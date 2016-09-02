
package org.talend.components.fullexample;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

/**
 * The FullExampleDefinition acts as an entry point for all of services that
 * a component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + FullExampleDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class FullExampleDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "FullExample"; //$NON-NLS-1$

    public FullExampleDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Examples" }; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
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
    public Class<? extends ComponentProperties> getPropertyClass() {
        return FullExampleProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology compType) {
        return null;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return Collections.emptySet();
    }

}
