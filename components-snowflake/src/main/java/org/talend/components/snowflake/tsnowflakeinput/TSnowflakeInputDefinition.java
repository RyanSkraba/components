package org.talend.components.snowflake.tsnowflakeinput;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeDefinition;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.components.snowflake.runtime.SnowflakeSource;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.EnumSet;
import java.util.Set;

/**
 * Component that can connect to a snowflake system and get some data out of it.
 */

public class TSnowflakeInputDefinition extends SnowflakeDefinition {

    public static final String COMPONENT_NAME = "tSnowflakeInput"; //$NON-NLS-1$

    public TSnowflakeInputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSnowflakeInputProperties.class;

        /* return SnowflakeConnectionProperties.class; //TODO: remove this */
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() { // TODO: Check for redundant
        // properties
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[]{SnowflakeTableProperties.class});
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ComponentProperties properties, ConnectorTopology componentType) {
        if (componentType == ConnectorTopology.OUTGOING) {
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), SnowflakeSource.class);
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

}
