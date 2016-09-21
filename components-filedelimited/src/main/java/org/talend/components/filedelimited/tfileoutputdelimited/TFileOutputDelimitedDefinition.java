package org.talend.components.filedelimited.tfileoutputdelimited;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.filedelimited.FileDelimitedDefinition;
import org.talend.components.filedelimited.runtime.FileDelimitedSink;
import org.talend.daikon.properties.Properties;

public class TFileOutputDelimitedDefinition extends FileDelimitedDefinition {

    public static final String COMPONENT_NAME = "tFileOutputDelimited"; //$NON-NLS-1$

    public TFileOutputDelimitedDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Output" }; //$NON-NLS-1$
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TFileOutputDelimitedProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology componentType) {
        if (componentType == ConnectorTopology.INCOMING || componentType == ConnectorTopology.INCOMING_AND_OUTGOING) {
            return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                    DependenciesReader.computeDependenciesFilePath(getMavenGroupId(), getMavenArtifactId()),
                    FileDelimitedSink.class.getCanonicalName());
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING);
    }
}
