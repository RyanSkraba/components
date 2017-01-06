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
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.jms.input.JmsInputDefinition;
import org.talend.components.jms.output.JmsOutputDefinition;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;

public class JmsDatastoreDefinition extends SimpleNamedThing implements DatastoreDefinition<JmsDatastoreProperties> {

    public static final String RUNTIME_1_1 = "org.talend.components.jms.runtime_1_1.DatastoreRuntime";

    public static final String NAME = "JmsDatastore";

    public JmsDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public Class getPropertiesClass() {
        return JmsDatastoreProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(JmsDatastoreProperties properties) {
        return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-jms/jms-runtime_1_1"),
                RUNTIME_1_1);
    }

    @Override
    public String getImagePath() {
        return null;
    }

    @Override
    public DatasetProperties createDatasetProperties(JmsDatastoreProperties storeProp) {
        JmsDatasetProperties setProp = new JmsDatasetProperties(JmsDatasetDefinition.NAME);
        setProp.init();
        setProp.setDatastoreProperties(storeProp);
        return setProp;
    }

    @Override
    public String getInputCompDefinitionName() {
        return JmsInputDefinition.COMPONENT_NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        return JmsOutputDefinition.COMPONENT_NAME;
    }

    @Override
    public String getDisplayName() {
        return getI18nMessage("datastore." + getName() + I18N_DISPLAY_NAME_SUFFIX);
    }

}
