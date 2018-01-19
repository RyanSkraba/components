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
package org.talend.components.google.drive;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionDefinition;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public abstract class GoogleDriveComponentDefinition extends AbstractComponentDefinition {

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_ARTIFACT_ID = "components-googledrive-runtime";

    public static final String MAVEN_RUNTIME_PATH = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_ARTIFACT_ID;

    public static final String SOURCE_OR_SINK_CLASS = "org.talend.components.google.drive.runtime.GoogleDriveSourceOrSink";

    public static final String SOURCE_CLASS = "org.talend.components.google.drive.runtime.GoogleDriveSource";

    public static final String SINK_CLASS = "org.talend.components.google.drive.runtime.GoogleDriveSink";

    public static final String COPY_RUNTIME_CLASS = "org.talend.components.google.drive.runtime.GoogleDriveCopyRuntime";

    public static final String CREATE_RUNTIME_CLASS = "org.talend.components.google.drive.runtime.GoogleDriveCreateRuntime";

    public static final String DELETE_RUNTIME_CLASS = "org.talend.components.google.drive.runtime.GoogleDriveDeleteRuntime";

    public static final String GET_RUNTIME_CLASS = "org.talend.components.google.drive.runtime.GoogleDriveGetRuntime";

    public static final String PUT_RUNTIME_CLASS = "org.talend.components.google.drive.runtime.GoogleDrivePutRuntime";

    // DataStreams & DataPrep
    public static final String DATASET_RUNTIME_CLASS = "org.talend.components.google.drive.runtime.data.GoogleDriveDatasetRuntime";

    public static final String DATASTORE_RUNTIME_CLASS = "org.talend.components.google.drive.runtime.data.GoogleDriveDatastoreRuntime";

    public static final String DATA_SOURCE_CLASS = "org.talend.components.google.drive.runtime.data.GoogleDriveDataSource";

    private static SandboxedInstanceProvider sandboxedInstanceProvider = SandboxedInstanceProvider.INSTANCE;

    public GoogleDriveComponentDefinition(String componentName) {
        super(componentName, ExecutionEngine.DI);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Cloud/Google Drive" };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { GoogleDriveConnectionProperties.class };
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        return this.getRuntimeInfo(engine, connectorTopology, GoogleDriveConnectionDefinition.SOURCE_CLASS);
    }

    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ConnectorTopology connectorTopology, String clazz) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        return getRuntimeInfo(clazz);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING, ConnectorTopology.NONE);
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    protected RuntimeInfo getRuntimeInfo(String className) {
        return new JarRuntimeInfo(MAVEN_RUNTIME_PATH,
                DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID), className);
    }

    public static RuntimeInfo getCommonRuntimeInfo(String clazz) {
        return new JarRuntimeInfo(MAVEN_RUNTIME_PATH,
                DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID), clazz);
    }

    public static class SandboxedInstanceProvider {

        public static final SandboxedInstanceProvider INSTANCE = new SandboxedInstanceProvider();

        public SandboxedInstance getSandboxedInstance(String runtimeClassName, boolean useCurrentJvmProperties) {
            ClassLoader classLoader = GoogleDriveComponentDefinition.class.getClassLoader();
            RuntimeInfo runtimeInfo = GoogleDriveComponentDefinition.getCommonRuntimeInfo(runtimeClassName);
            if (useCurrentJvmProperties) {
                return RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(runtimeInfo, classLoader);
            } else {
                return RuntimeUtil.createRuntimeClass(runtimeInfo, classLoader);
            }
        }
    }

    public static void setSandboxedInstanceProvider(SandboxedInstanceProvider provider) {
        sandboxedInstanceProvider = provider;
    }

    public static SandboxedInstanceProvider getSandboxedInstanceProvider() {
        return sandboxedInstanceProvider;
    }

    public static SandboxedInstance getSandboxedInstance(String runtimeClassName) {
        return getSandboxedInstance(runtimeClassName, false);
    }

    public static SandboxedInstance getSandboxedInstance(String runtimeClassName, boolean useCurrentJvmProperties) {
        return sandboxedInstanceProvider.getSandboxedInstance(runtimeClassName, useCurrentJvmProperties);
    }

}
