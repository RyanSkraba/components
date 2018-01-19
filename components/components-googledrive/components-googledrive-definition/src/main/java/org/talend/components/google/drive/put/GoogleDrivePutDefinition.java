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
package org.talend.components.google.drive.put;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.components.google.drive.connection.GoogleDriveConnectionDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDrivePutDefinition extends GoogleDriveComponentDefinition {

    public static final String COMPONENT_NAME = "tGoogleDrivePut";

    public static final String RETURN_CONTENT = "content";

    public static final String RETURN_PARENT_FOLDER_ID = "parentFolderId"; //$NON-NLS-1$

    public static final String RETURN_FILE_ID = "fileId";

    public static final Property<String> RETURN_PARENT_FOLDER_ID_PROP = PropertyFactory.newString(RETURN_PARENT_FOLDER_ID);

    public static final Property<String> RETURN_FILE_ID_PROP = PropertyFactory.newString(RETURN_FILE_ID);

    public GoogleDrivePutDefinition() {
        super(COMPONENT_NAME);
        setupI18N(new Property<?>[] { RETURN_PARENT_FOLDER_ID_PROP, RETURN_FILE_ID_PROP });
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_PARENT_FOLDER_ID_PROP, RETURN_FILE_ID_PROP };
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return GoogleDrivePutProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        assertConnectorTopologyCompatibility(connectorTopology);
        switch (connectorTopology) {
        case NONE:
            return getRuntimeInfo(GoogleDriveConnectionDefinition.PUT_RUNTIME_CLASS);
        case OUTGOING:
            return getRuntimeInfo(GoogleDriveConnectionDefinition.SOURCE_CLASS);
        case INCOMING:
        case INCOMING_AND_OUTGOING:
            return getRuntimeInfo(GoogleDriveConnectionDefinition.SINK_CLASS);
        }
        return null;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING, ConnectorTopology.NONE,
                ConnectorTopology.OUTGOING);
    }

}
