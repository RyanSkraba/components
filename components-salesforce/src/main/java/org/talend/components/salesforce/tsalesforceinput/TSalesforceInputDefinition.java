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
package org.talend.components.salesforce.tsalesforceinput;

import org.springframework.stereotype.Component;
import org.talend.components.base.ComponentConnector;
import org.talend.components.base.ComponentConnector.Type;
import org.talend.components.base.ComponentDefinition;
import org.talend.components.base.ComponentProperties;
import org.talend.components.base.Constants;
import org.talend.components.salesforce.SalesforceConnectionProperties;

/**
 * Component that can connect to a salesforce system and get some data out of it.
 */
@Component(Constants.COMPONENT_BEAN_PREFIX + TSalesforceInputDefinition.COMPONENT_NAME)
public class TSalesforceInputDefinition extends ComponentDefinition {

    public static final String               COMPONENT_NAME = "tSalesforceInput";                       //$NON-NLS-1$

    protected ComponentConnector[]           connectors     = { new ComponentConnector(Type.FLOW, 0, 0),
            new ComponentConnector(Type.ITERATE, 1, 0), new ComponentConnector(Type.SUBJOB_OK, 1, 0),
            new ComponentConnector(Type.SUBJOB_ERROR, 1, 0) };

    protected SalesforceConnectionProperties properties;

    @Override
    public ComponentProperties createProperties() {
        return new TSalesforceInputProperties();
    }

    @Override
    public Family[] getSupportedFamilies() {
        return new Family[] { Family.BUSINESS, Family.CLOUD };
    }

    // @Override
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

}
