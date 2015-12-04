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
package org.talend.components.salesforce.tsalesforceoutput;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentConnector.ConnectorType;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentTrigger;
import org.talend.components.api.component.ComponentTrigger.TriggerType;
import org.talend.components.salesforce.SalesforceDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Component that can connect to a salesforce system and put some data into it.
 */

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceOutputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceOutputDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceOutputNew"; //$NON-NLS-1$

    public TSalesforceOutputDefinition() {
        super(COMPONENT_NAME);

        setConnectors(new ComponentConnector(ConnectorType.FLOW, 1, 0), new ComponentConnector(ConnectorType.MAIN, 0, 1),
                new ComponentConnector(ConnectorType.REJECT, 0, 1));
        setTriggers(new ComponentTrigger(TriggerType.SUBJOB_OK, 1, 0), new ComponentTrigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public String getPartitioning() {
        return AUTO;
    }

    @Override
    public Class<?> getPropertyClass() {
        return TSalesforceOutputProperties.class;
    }

}
