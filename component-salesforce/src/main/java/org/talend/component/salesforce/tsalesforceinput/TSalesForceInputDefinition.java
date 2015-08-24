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
package org.talend.component.salesforce.tsalesforceinput;

import org.springframework.stereotype.Component;
import org.talend.component.ComponentConnector;
import org.talend.component.ComponentConnector.Type;
import org.talend.component.ComponentDefinition;
import org.talend.component.ComponentProperties;
import org.talend.component.Constants;
import org.talend.component.salesforce.SalesforceConnectionProperties;

/**
 * Component that can connect to a salesforce system and get some data out of it.
 *
 */
@Component(Constants.COMPONENT_BEAN_PREFIX + TSalesForceInputDefinition.COMPONENT_NAME)
public class TSalesForceInputDefinition extends ComponentDefinition {

    public static final String COMPONENT_NAME = "SalesforceInput"; //$NON-NLS-1$

    protected ComponentConnector[] connectors = { new ComponentConnector(Type.FLOW, 0, 0),
            new ComponentConnector(Type.ITERATE, 1, 0), new ComponentConnector(Type.SUBJOB_OK, 1, 0),
            new ComponentConnector(Type.SUBJOB_ERROR, 1, 0) };

    protected SalesforceConnectionProperties properties;

    @Override
    public ComponentProperties createProperties() {
        return new TSalesForceInputProperties();
    }

    @Override
    public Family[] getSupportedFamilies() {
        return new Family[] { Family.BUSINESS, Family.CLOUD };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.component.ComponentDefinition#getName()
     */
    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

}
