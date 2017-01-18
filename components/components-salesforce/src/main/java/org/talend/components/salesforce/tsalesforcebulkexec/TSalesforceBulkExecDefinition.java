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
package org.talend.components.salesforce.tsalesforcebulkexec;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.runtime.SalesforceSource;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class TSalesforceBulkExecDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceBulkExec"; //$NON-NLS-1$

    public TSalesforceBulkExecDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI);
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return false;
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSalesforceBulkExecProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { SalesforceModuleProperties.class });
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_SUCCESS_RECORD_COUNT_PROP,
                RETURN_REJECT_RECORD_COUNT_PROP };
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology componentType) {
        assertEngineCompatibility(engine);
        if (componentType == ConnectorTopology.OUTGOING || componentType == ConnectorTopology.NONE) {
            return getCommonRuntimeInfo(this.getClass().getClassLoader(), SalesforceSource.class);
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

}
