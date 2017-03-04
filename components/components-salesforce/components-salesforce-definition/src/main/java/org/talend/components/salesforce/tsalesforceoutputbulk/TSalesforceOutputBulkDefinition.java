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
package org.talend.components.salesforce.tsalesforceoutputbulk;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class TSalesforceOutputBulkDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceOutputBulk"; //$NON-NLS-1$

    public TSalesforceOutputBulkDefinition() {
        super(COMPONENT_NAME, ExecutionEngine.DI);
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

    @Override
    public String getPartitioning() {
        return NONE;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSalesforceOutputBulkProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { SchemaProperties.class });
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology componentType) {
        assertEngineCompatibility(engine);
        if (componentType == ConnectorTopology.INCOMING) {
            return getCommonRuntimeInfo("org.talend.components.salesforce.runtime.SalesforceBulkFileSink");
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }
}
