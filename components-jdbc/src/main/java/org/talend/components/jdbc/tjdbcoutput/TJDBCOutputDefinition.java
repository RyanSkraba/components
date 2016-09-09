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
package org.talend.components.jdbc.tjdbcoutput;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCOutputDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "tJDBCOutputNew";

    public TJDBCOutputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCOutputProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB_JDBC" };
    }

    // TODO can't i18n
    public static final String RETURN_INSERT_RECORD_COUNT = "insertRecordCount";

    public static final String RETURN_UPDATE_RECORD_COUNT = "updateRecordCount";

    public static final String RETURN_DELETE_RECORD_COUNT = "deleteRecordCount";

    public static final Property<Integer> RETURN_INSERT_RECORD_COUNT_PROP = PropertyFactory
            .newInteger(RETURN_INSERT_RECORD_COUNT);

    public static final Property<Integer> RETURN_UPDATE_RECORD_COUNT_PROP = PropertyFactory
            .newInteger(RETURN_UPDATE_RECORD_COUNT);

    public static final Property<Integer> RETURN_DELETE_RECORD_COUNT_PROP = PropertyFactory
            .newInteger(RETURN_DELETE_RECORD_COUNT);

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_SUCCESS_RECORD_COUNT_PROP,
                RETURN_REJECT_RECORD_COUNT_PROP, RETURN_INSERT_RECORD_COUNT_PROP, RETURN_UPDATE_RECORD_COUNT_PROP,
                RETURN_DELETE_RECORD_COUNT_PROP };
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology connectorTopology) {
        if (connectorTopology == ConnectorTopology.INCOMING || connectorTopology == ConnectorTopology.INCOMING_AND_OUTGOING) {
            return new SimpleRuntimeInfo(this.getClass().getClassLoader(), "org.talend.components", "components-jdbc",
                    JDBCSink.class.getCanonicalName());
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING);
    }

}
