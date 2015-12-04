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
import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentConnector.ConnectorType;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentTrigger;
import org.talend.components.api.component.ComponentTrigger.TriggerType;
import org.talend.components.salesforce.SalesforceDefinition;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceOutputBulkExecDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceOutputBulkExecDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceOutputBulkExecNew"; //$NON-NLS-1$

    public TSalesforceOutputBulkExecDefinition() {
        super(COMPONENT_NAME);

        setConnectors(new ComponentConnector(ConnectorType.FLOW, 0, 0));
        setTriggers(new ComponentTrigger(TriggerType.ITERATE, 1, 0), new ComponentTrigger(TriggerType.SUBJOB_OK, 1, 0),
                new ComponentTrigger(TriggerType.SUBJOB_ERROR, 1, 0));
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
    public Class<?> getPropertyClass() {
        return TSalesforceOutputBulkExecProperties.class;
    }

}
