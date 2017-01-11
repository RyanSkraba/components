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

import java.net.MalformedURLException;
import java.net.URL;

import ${packageTalend}.api.component.runtime.DependenciesReader;
import ${packageTalend}.api.component.runtime.JarRuntimeInfo;
import ${packageTalend}.api.exception.ComponentException;
import ${packageTalend}.common.dataset.DatasetDefinition;
import ${packageDaikon}.definition.I18nDefinition;
import ${packageDaikon}.runtime.RuntimeInfo;


public class ${componentNameClass}DatasetDefinition extends I18nDefinition implements DatasetDefinition<${componentNameClass}DatasetProperties> {

    public static final String RUNTIME${runtimeVersionConverted} = "org.talend.components.${componentNameLowerCase}.runtime${runtimeVersionConverted}.${componentNameClass}DatasetRuntime";

    public static final String NAME = ${componentNameClass}ComponentFamilyDefinition.NAME + "Dataset";

    public ${componentNameClass}DatasetDefinition() {
        super(NAME);
    }

    @Override
    public Class<${componentNameClass}DatasetProperties> getPropertiesClass() {
        return ${componentNameClass}DatasetProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(${componentNameClass}DatasetProperties properties) {
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/${componentNameLowerCase}-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "${componentNameLowerCase}-runtime"), RUNTIME${runtimeVersionConverted});
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

}
