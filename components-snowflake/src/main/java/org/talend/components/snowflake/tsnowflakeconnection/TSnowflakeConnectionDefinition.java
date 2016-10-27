package org.talend.components.snowflake.tsnowflakeconnection;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeDefinition;
import org.talend.components.snowflake.runtime.SnowflakeSourceOrSink;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.EnumSet;
import java.util.Set;

public class TSnowflakeConnectionDefinition extends SnowflakeDefinition {

    public static final String COMPONENT_NAME = "tSnowflakeConnection"; //$NON-NLS-1$

    public TSnowflakeConnectionDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return SnowflakeConnectionProperties.class;
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[]{RETURN_ERROR_MESSAGE_PROP};
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology componentType) {
        if (componentType == ConnectorTopology.NONE) {
            return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                    DependenciesReader.computeDependenciesFilePath(getMavenGroupId(), getMavenArtifactId()),
                    SnowflakeSourceOrSink.class.getCanonicalName());
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.NONE);
    }

}
