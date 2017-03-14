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
package org.talend.components.marketo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

public abstract class MarketoComponentDefinition extends AbstractComponentDefinition {

    public static final String MAVEN_DEFINITION_ARTIFACT_ID = "marketo-definition";

    public static final String MAVEN_RUNTIME_ARTIFACT_ID = "marketo-runtime";

    public static final String MAVEN_PATH = "mvn:org.talend.components/components-marketo";

    public static final String MAVEN_RUNTIME_PATH = "mvn:org.talend.components/marketo-runtime";

    public static final String MAVEN_ARTIFACT_ID = "components-marketo";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String RUNTIME_SINK_CLASS = "org.talend.components.marketo.runtime.MarketoSink";

    public static final String RUNTIME_SOURCEORSINK_CLASS = "org.talend.components.marketo.runtime.MarketoSourceOrSink";

    public static final String RUNTIME_SOURCE_CLASS = "org.talend.components.marketo.runtime.MarketoSource";

    public static final String RETURN_NB_CALL = "numberCall";

    public static final Property<Integer> RETURN_NB_CALL_PROP = PropertyFactory.newInteger(RETURN_NB_CALL);

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoComponentDefinition.class);

    public MarketoComponentDefinition(String componentName) {
        super(componentName, ExecutionEngine.DI);
        setupI18N(new Property<?>[] { RETURN_NB_CALL_PROP });
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_NB_CALL_PROP };
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Business/Marketo", "Cloud/Marketo" };
    }

    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { TMarketoConnectionProperties.class };
    }

    public static RuntimeInfo getCommonRuntimeInfo(ClassLoader classLoader, String clazz) {
        return new SimpleRuntimeInfo(classLoader,
                DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID), clazz);
    }

}
