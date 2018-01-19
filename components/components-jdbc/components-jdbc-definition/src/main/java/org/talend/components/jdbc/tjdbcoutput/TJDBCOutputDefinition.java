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
package org.talend.components.jdbc.tjdbcoutput;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.components.jdbc.wizard.JDBCConnectionWizardProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * JDBC output component
 *
 */
public class TJDBCOutputDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "tJDBCOutput";

    public TJDBCOutputDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI);
        setupI18N(new Property<?>[] { RETURN_INSERT_RECORD_COUNT_PROP, RETURN_UPDATE_RECORD_COUNT_PROP,
                RETURN_DELETE_RECORD_COUNT_PROP, RETURN_COMMON_REJECT_RECORD_COUNT_PROP, RETURN_QUERY_PROP });
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCOutputProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB_JDBC" };
    }

    public static final Property<Integer> RETURN_INSERT_RECORD_COUNT_PROP = PropertyFactory
            .newInteger(ComponentConstants.RETURN_INSERT_RECORD_COUNT);

    public static final Property<Integer> RETURN_UPDATE_RECORD_COUNT_PROP = PropertyFactory
            .newInteger(ComponentConstants.RETURN_UPDATE_RECORD_COUNT);

    public static final Property<Integer> RETURN_DELETE_RECORD_COUNT_PROP = PropertyFactory
            .newInteger(ComponentConstants.RETURN_DELETE_RECORD_COUNT);

    public static final Property<Integer> RETURN_COMMON_REJECT_RECORD_COUNT_PROP = PropertyFactory
            .newInteger(ComponentConstants.RETURN_REJECT_RECORD_COUNT);

    public static final Property<String> RETURN_QUERY_PROP = PropertyFactory.newString(ComponentConstants.RETURN_QUERY);

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_INSERT_RECORD_COUNT_PROP,
                RETURN_UPDATE_RECORD_COUNT_PROP, RETURN_DELETE_RECORD_COUNT_PROP, RETURN_COMMON_REJECT_RECORD_COUNT_PROP,
                RETURN_QUERY_PROP };
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        if (connectorTopology == ConnectorTopology.INCOMING || connectorTopology == ConnectorTopology.INCOMING_AND_OUTGOING) {
            return new JdbcRuntimeInfo((TJDBCOutputProperties) properties, "org.talend.components.jdbc.runtime.JDBCSink");
        }
        return null;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING);
    }

    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { JDBCConnectionWizardProperties.class};
    }

    @Override
    public boolean isParallelize() {
        return true;
    }

}
