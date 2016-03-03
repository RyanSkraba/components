#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import java.io.InputStream;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;

import aQute.bnd.annotation.component.Component;

/**
 * The ${componentName}Definition acts as an entry point for all of services that 
 * a component provides to integrate with the Studio (at design-time) and other 
 * components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + ${componentName}Definition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class ${componentName}Definition extends AbstractComponentDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "${componentName}"; //$NON-NLS-1$


    public ${componentName}Definition() {
        setConnectors(new Connector(ConnectorType.FLOW, 0, 1));
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 1), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/input" }; //$NON-NLS-1$
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
    public InputStream getMavenPom() {
        return this.getClass().getResourceAsStream("pom.xml"); //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return ${componentName}Properties.class;
    }

    @Override
    public Source getRuntime() {
        return new ${componentName}Source();
    }
}
