package org.talend.components.google.drive.data;

import static org.talend.components.google.drive.GoogleDriveComponentDefinition.DATA_SOURCE_CLASS;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.SupportedProduct;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDriveInputDefinition extends AbstractComponentDefinition {

    public static final String NAME = "GoogleDriveDataInput";

    public GoogleDriveInputDefinition() {
        super(NAME, ExecutionEngine.DI);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return GoogleDriveInputProperties.class;
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        return GoogleDriveComponentDefinition.getCommonRuntimeInfo(DATA_SOURCE_CLASS);
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public List<String> getSupportedProducts() {
        return Arrays.asList(SupportedProduct.DATAPREP, SupportedProduct.DATASTREAMS);
    }
}
