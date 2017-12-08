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

package org.talend.components.netsuite;

import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.java8.Function;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Base class for definitions of NetSuite components.
 */
public abstract class NetSuiteComponentDefinition extends AbstractComponentDefinition {

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_ARTIFACT_ID = "components-netsuite-runtime_${version}";

    public static final String SOURCE_OR_SINK_CLASS =
            "org.talend.components.netsuite.v${version}.NetSuiteSourceOrSinkImpl";

    public static final String SOURCE_CLASS =
            "org.talend.components.netsuite.v${version}.NetSuiteSourceImpl";

    public static final String SINK_CLASS =
            "org.talend.components.netsuite.v${version}.NetSuiteSinkImpl";

    public static final String RUNTIME_CLASS =
            "org.talend.components.netsuite.v${version}.NetSuiteRuntimeImpl";

    /** Responsible for invocation of NetSuite component runtime. */
    private static RuntimeInvoker runtimeInvoker = new SandboxRuntimeInvoker();

    protected NetSuiteComponentDefinition(String componentName, ExecutionEngine engine1, ExecutionEngine... engines) {
        super(componentName, engine1, engines);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Business/NetSuite", "Cloud/NetSuite" };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { NetSuiteConnectionProperties.class };
    }

    @Override
    // Most of the components are on the input side, so put this here, the output definition will override this
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP };
    }

    /**
     * Perform an operation using {@link NetSuiteDatasetRuntime}.
     *
     * @param properties properties object which can provide connection properties
     * @param func an operation
     * @param <R> type of operation result
     * @return result of operation
     */
    public static <R> R withDatasetRuntime(final NetSuiteProvideConnectionProperties properties,
            final Function<NetSuiteDatasetRuntime, R> func) {
        return withRuntime(properties, new Function<NetSuiteRuntime, R>() {
            @Override public R apply(NetSuiteRuntime runtime) {
                NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(properties);
                return func.apply(dataSetRuntime);
            }
        });
    }

    /**
     * Perform an operation using {@link NetSuiteRuntime}.
     *
     * @param properties properties object which can provide connection properties
     * @param func an operation
     * @param <R> type of operation result
     * @return result of operation
     */
    public static <R> R withRuntime(final NetSuiteProvideConnectionProperties properties,
            final Function<NetSuiteRuntime, R> func) {

        NetSuiteConnectionProperties connectionProperties = properties.getConnectionProperties();
        return runtimeInvoker.invokeRuntime(connectionProperties.getDesignTimeContext(),
                properties, func);
    }

    /**
     * Get {@code RuntimeInfo} using given {@code NetSuiteProvideConnectionProperties} and
     * name of target runtime class.
     *
     * @param properties properties object which can provide connection properties
     * @param runtimeClassName name of target runtime class
     * @return {@code RuntimeInfo}
     */
    public static RuntimeInfo getRuntimeInfo(final NetSuiteProvideConnectionProperties properties,
            final String runtimeClassName) {

        NetSuiteConnectionProperties connectionProperties = properties.getConnectionProperties();

        try {
            NetSuiteVersion version = connectionProperties.getApiVersion();
            return getRuntimeInfo(version, runtimeClassName);
        } catch (IllegalArgumentException e) {
            throw new ComponentException(e);
        }
    }

    /**
     * Get {@code RuntimeInfo} using given target version of NetSuite and
     * name of target runtime class.
     *
     * @param version target version of NetSuite
     * @param runtimeClassName name of target runtime class
     * @return {@code RuntimeInfo}
     */
    public static RuntimeInfo getRuntimeInfo(final NetSuiteVersion version, final String runtimeClassName) {
        String versionString = version.getMajorAsString("_");
        String artifactId = MAVEN_ARTIFACT_ID.replace("${version}", versionString);
        String className = runtimeClassName.replace("${version}", versionString);
        return new JarRuntimeInfo("mvn:" + MAVEN_GROUP_ID + "/" + artifactId,
                DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, artifactId),
                className);
    }

    public static RuntimeInvoker getRuntimeInvoker() {
        return runtimeInvoker;
    }

    /**
     * Set invoker of operations which require {@link NetSuiteRuntime}.
     *
     * <p>This method is intended for testing purposes only.
     *
     * @param runtimeInvoker invoker to be set
     */
    public static void setRuntimeInvoker(RuntimeInvoker runtimeInvoker) {
        if (runtimeInvoker == null) {
            throw new IllegalArgumentException("Runtime invoker can't be null");
        }
        NetSuiteComponentDefinition.runtimeInvoker = runtimeInvoker;
    }

    /**
     * Holds design-time related data for a component.
     */
    public static class DesignTimeContext implements NetSuiteRuntime.Context {
        protected Map<String, Object> attributes = new HashMap<>();

        @Override
        public boolean isCachingEnabled() {
            return true;
        }

        @Override
        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        @Override
        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
    }

    /**
     * Invoker of an operation which require {@link NetSuiteRuntime}.
     */
    public interface RuntimeInvoker {

        /**
         * Invoke an operation using {@link NetSuiteRuntime}.
         *
         * @param context to be passed to {@link NetSuiteRuntime}
         * @param properties connection properties object
         * @param func operation to be performed
         * @param <R> type of operation result
         * @return result of operation
         */
        <R> R invokeRuntime(NetSuiteRuntime.Context context,
                NetSuiteProvideConnectionProperties properties,
                Function<NetSuiteRuntime, R> func);
    }

    /**
     * Invokes operations using sanboxing mechanism.
     */
    public static class SandboxRuntimeInvoker implements RuntimeInvoker {

        @Override
        public <R> R invokeRuntime(final NetSuiteRuntime.Context context,
                final NetSuiteProvideConnectionProperties properties,
                final Function<NetSuiteRuntime, R> func) {

            RuntimeInfo runtimeInfo = getRuntimeInfo(properties, RUNTIME_CLASS);
            try (SandboxedInstance sandboxI = RuntimeUtil.createRuntimeClass(runtimeInfo,
                    NetSuiteComponentDefinition.class.getClassLoader())) {
                NetSuiteRuntime runtime = (NetSuiteRuntime) sandboxI.getInstance();
                runtime.setContext(context);
                return func.apply(runtime);
            }
        }
    }
}
