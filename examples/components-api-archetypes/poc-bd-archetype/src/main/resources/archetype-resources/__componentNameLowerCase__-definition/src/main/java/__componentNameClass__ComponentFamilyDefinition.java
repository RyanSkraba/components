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

import ${packageTalend}.api.AbstractComponentFamilyDefinition;
import ${packageTalend}.api.ComponentInstaller;
import ${packageTalend}.api.Constants;
import ${package}.input.${componentNameClass}InputDefinition;
import ${package}.output.${componentNameClass}OutputDefinition;

import aQute.bnd.annotation.component.Component;

import com.google.auto.service.AutoService;

/**
 * Install all of the definitions provided for the ${componentName} family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX
        + ${componentNameClass}ComponentFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class ${componentNameClass}ComponentFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "${componentName}";

    public ${componentNameClass}ComponentFamilyDefinition() {
        super(NAME, new ${componentNameClass}DatastoreDefinition(), new ${componentNameClass}DatasetDefinition(), new ${componentNameClass}InputDefinition(),
                new ${componentNameClass}OutputDefinition());
    }

    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
