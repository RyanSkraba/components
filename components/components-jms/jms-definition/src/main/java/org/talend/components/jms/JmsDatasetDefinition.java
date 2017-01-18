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

package org.talend.components.jms;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class JmsDatasetDefinition extends I18nDefinition implements DatasetDefinition<JmsDatasetProperties> {

    public static final String RUNTIME_1_1 = "org.talend.components.jms.runtime_1_1.DatasetRuntime";

    public static final String NAME = "JmsDataset";

    public JmsDatasetDefinition() {
        super(NAME);
    }

    @Override
    public Class getPropertiesClass() {
        return JmsDatasetProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(JmsDatasetProperties properties) {
        return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-jms/jms-runtime_1_1"),
                RUNTIME_1_1);
    }

    @Override
    public String getImagePath() {
        return "/org/talend/components/jms/input/JmsInput_icon32.png";
    }

}
