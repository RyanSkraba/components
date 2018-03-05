// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.processing.definition;

import aQute.bnd.annotation.component.Component;
import com.google.auto.service.AutoService;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.processing.definition.aggregate.AggregateDefinition;
import org.talend.components.processing.definition.fieldselector.FieldSelectorDefinition;
import org.talend.components.processing.definition.filterrow.FilterRowDefinition;
import org.talend.components.processing.definition.limit.LimitDefinition;
import org.talend.components.processing.definition.normalize.NormalizeDefinition;
import org.talend.components.processing.definition.pythonrow.PythonRowDefinition;
import org.talend.components.processing.definition.replicate.ReplicateDefinition;
import org.talend.components.processing.definition.typeconverter.TypeConverterDefinition;
import org.talend.components.processing.definition.window.WindowDefinition;

/**
 * Install all of the definitions provided for the processing family of
 * components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + ProcessingFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class ProcessingFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Processing";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_ARTIFACT_ID = "processing-runtime";

    public ProcessingFamilyDefinition() {
        super(NAME,
                // Components
                new AggregateDefinition(), new FieldSelectorDefinition(), new FilterRowDefinition(), new LimitDefinition(),
                new NormalizeDefinition(), new PythonRowDefinition(), new ReplicateDefinition(), new TypeConverterDefinition(),
                new WindowDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }

    public static String computeDependenciesFilepath() {
        return DependenciesReader.computeDependenciesFilePath(ProcessingFamilyDefinition.MAVEN_GROUP_ID,
                ProcessingFamilyDefinition.MAVEN_ARTIFACT_ID);
    }
}
