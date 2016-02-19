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
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.runtime.ComponentRuntime;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + ${componentName}Definition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class ${componentName}Definition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "${componentName}"; //$NON-NLS-1$

    
    public ${componentName}Definition() {
        setConnectors(new Connector(ConnectorType.FLOW, 0, 1));
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 1), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/input" };
    }
    
    @Override
    public ComponentRuntime createRuntime() {
        return new ${componentName}Runtime();
    }

    
    @Override
    public boolean isStartable() {
        return true;
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
        return this.getClass().getResourceAsStream("pom.xml");
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public Class<?> getPropertyClass() {
        return ${componentName}Properties.class;
    }
}
