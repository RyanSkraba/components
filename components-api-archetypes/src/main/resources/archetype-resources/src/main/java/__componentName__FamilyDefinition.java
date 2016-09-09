#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.i18n.I18nMessages;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the ${componentName} family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + ${componentName}FamilyDefinition.NAME, provide = ComponentInstaller.class)
public class ${componentName}FamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "${componentName}";

    public ${componentName}FamilyDefinition() {
        super(NAME, new ${componentName}Definition());

    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}