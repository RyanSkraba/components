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
package org.talend.components.salesforce.tsalesforceoutputbulkexec;

import org.talend.components.api.Constants;
import org.talend.components.api.component.*;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceDefinition;

import aQute.bnd.annotation.component.Component;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceOutputBulkExecDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceOutputBulkExecDefinition extends SalesforceDefinition implements VirtualComponentDefinition {

    public static final String COMPONENT_NAME = "tSalesforceOutputBulkExec"; //$NON-NLS-1$

    public TSalesforceOutputBulkExecDefinition() {
        super(COMPONENT_NAME);
        setConnectors(new Connector(ConnectorType.FLOW, 1, 0), new Connector(ConnectorType.MAIN, 0, 1),
                new Connector(ConnectorType.REJECT, 0, 1));
        setTriggers(new Trigger(TriggerType.ITERATE, 0, 0), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public String getPartitioning() {
        return NONE;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSalesforceOutputBulkExecProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { SalesforceModuleProperties.class });
    }

    @Override
    public ComponentDefinition getInputComponentDefinition() {
        return new TSalesforceOutputBulkDefinition();
    }

    @Override
    public ComponentDefinition getOutputComponentDefinition() {
        return new TSalesforceBulkExecDefinition();
    }
}
