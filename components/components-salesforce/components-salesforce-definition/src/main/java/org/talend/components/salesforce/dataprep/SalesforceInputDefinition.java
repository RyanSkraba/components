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
package org.talend.components.salesforce.dataprep;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * the salesforce input component which work with data store and data set
 * 
 */
public class SalesforceInputDefinition extends AbstractComponentDefinition {

    public static String NAME = "DataPrepSalesforceInput";

    public SalesforceInputDefinition() {
        super(NAME, ExecutionEngine.DI);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return SalesforceInputProperties.class;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties,
            ConnectorTopology connectorTopology) {
        assertEngineCompatibility(engine);
        return SalesforceDefinition.getCommonRuntimeInfo("org.talend.components.salesforce.runtime.dataprep.SalesforceDataprepSource");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }
}
