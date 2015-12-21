#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

        package ${package};

import java.io.InputStream;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.runtime.ComponentRuntime;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + ${classNamePrefix}Definition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class ${classNamePrefix}Definition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "${classNamePrefix}"; //$NON-NLS-1$

    @Override
    public ComponentRuntime createRuntime() {
        return new ${classNamePrefix}Runtime();
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
        return null;
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public Class<?> getPropertyClass() {
        return ${classNamePrefix}Properties.class;
    }
}
