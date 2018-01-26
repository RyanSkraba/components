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
package org.talend.components.jdbc.tjdbcinput;

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
 * JDBC input component
 *
 */
public class TJDBCInputDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "tJDBCInput";

    public TJDBCInputDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI);
        setupI18N(new Property<?>[] { RETURN_QUERY_PROP });
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJDBCInputProperties.class;
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Databases/DB Specifics/JDBC" };
    }

    public static final Property<String> RETURN_QUERY_PROP = PropertyFactory.newString(ComponentConstants.RETURN_QUERY);

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_QUERY_PROP };
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        return new JdbcRuntimeInfo((TJDBCInputProperties) properties, "org.talend.components.jdbc.runtime.JDBCSource");
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { JDBCConnectionWizardProperties.class };
    }

}
