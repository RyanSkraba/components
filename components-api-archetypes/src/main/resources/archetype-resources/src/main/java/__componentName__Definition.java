#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

/**
 * The ${componentName}Definition acts as an entry point for all of services that 
 * a component provides to integrate with the Studio (at design-time) and other 
 * components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + ${componentName}Definition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class ${componentName}Definition extends AbstractComponentDefinition {
    public static final String COMPONENT_NAME = "${componentName}"; //$NON-NLS-1$

    public ${componentName}Definition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Input" }; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { };
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        default:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        }
    }
    
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return ${componentName}Properties.class;
    }

    
    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology componentType) {
        if (componentType == ConnectorTopology.OUTGOING) {
            return new SimpleRuntimeInfo(this.getClass().getClassLoader(), "${groupId}", "${artifactId}", ${componentName}Source.class.getCanonicalName());
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    } 
    
}
