// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.tjdbcrow;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX + TJDBCRowDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TJDBCRowDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "tJDBCRowNew";

    public TJDBCRowDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCRowProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB_JDBC" };
    }

    // TODO add more return properties
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology connectorTopology) {
        return null;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }

}
