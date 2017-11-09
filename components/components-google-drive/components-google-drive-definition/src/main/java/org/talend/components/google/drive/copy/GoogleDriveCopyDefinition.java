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
package org.talend.components.google.drive.copy;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.components.google.drive.connection.GoogleDriveConnectionDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDriveCopyDefinition extends GoogleDriveComponentDefinition {

    public static final String COMPONENT_NAME = "tGoogleDriveCopy";//$NON-NLS-1$

    public static final String RETURN_SOURCE_ID = "sourceId"; //$NON-NLS-1$

    public static final Property<String> RETURN_SOURCE_ID_PROP = PropertyFactory.newString(RETURN_SOURCE_ID);

    public static final String RETURN_DESTINATION_ID = "destinationId"; //$NON-NLS-1$

    public static final Property<String> RETURN_DESTINATION_ID_PROP = PropertyFactory.newString(RETURN_DESTINATION_ID);

    public GoogleDriveCopyDefinition() {
        super(COMPONENT_NAME);
        setupI18N(new Property<?>[] { RETURN_SOURCE_ID_PROP, RETURN_DESTINATION_ID_PROP });
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_SOURCE_ID_PROP, RETURN_DESTINATION_ID_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        if (ConnectorTopology.NONE.equals(connectorTopology)) {
            return getRuntimeInfo(GoogleDriveConnectionDefinition.COPY_RUNTIME_CLASS);
        } else if (ConnectorTopology.OUTGOING.equals(connectorTopology)) {
            return getRuntimeInfo(GoogleDriveConnectionDefinition.SOURCE_CLASS);
        } else {
            return null;
        }
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return GoogleDriveCopyProperties.class;
    }

}
