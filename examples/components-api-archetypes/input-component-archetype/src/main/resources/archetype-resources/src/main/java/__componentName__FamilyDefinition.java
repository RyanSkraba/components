#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
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
