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
package org.talend.components.salesforce.tsalesforceoutputbulk;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentConnector.Type;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.salesforce.SalesforceDefinition;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceOutputBulkDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceOutputBulkDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceOutputBulkNew"; //$NON-NLS-1$

    public TSalesforceOutputBulkDefinition() {
        super(COMPONENT_NAME);
        setConnectors(new ComponentConnector(Type.FLOW, 1, 0),new ComponentConnector(Type.SUBJOB_OK, 1, 0),
               new ComponentConnector(Type.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public String getPartitioning() {
        return NONE;
    }

    @Override
    public Class<?> getPropertyClass() {
        return TSalesforceOutputBulkProperties.class;
    }

}
