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
package org.talend.components.salesforce.tsalesforcewaveoutputbulkexec;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentConnector.Type;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.salesforce.SalesforceDefinition;

@org.springframework.stereotype.Component(Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceWaveOutputBulkExecDefinition.COMPONENT_NAME)
@aQute.bnd.annotation.component.Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceWaveOutputBulkExecDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceWaveOutputBulkExecDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceWaveOutputBulkExec"; //$NON-NLS-1$

    public TSalesforceWaveOutputBulkExecDefinition() {
        super(COMPONENT_NAME);
        propertiesClass = TSalesforceWaveOutputBulkExecProperties.class;
        setConnectors(new ComponentConnector(Type.FLOW, 0, 0), new ComponentConnector(Type.ITERATE, 1, 0),
                new ComponentConnector(Type.SUBJOB_OK, 1, 0), new ComponentConnector(Type.SUBJOB_ERROR, 1, 0));
    }

    public boolean isConditionalInputs() {
        return true;
    }

    public boolean isStartable() {
        return false;
    }

    public String getPartitioning() {
        return NONE;
    }

}
