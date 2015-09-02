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
package org.talend.components.salesforce.tsalesforceconnect;

import org.springframework.stereotype.Component;
import org.talend.components.base.ComponentConnector;
import org.talend.components.base.ComponentConnector.Type;
import org.talend.components.base.ComponentDefinition;
import org.talend.components.base.ComponentProperties;
import org.talend.components.base.Constants;
import org.talend.components.salesforce.SalesforceConnectionProperties;

@Component(Constants.COMPONENT_BEAN_PREFIX + TSalesforceConnectDefinition.COMPONENT_NAME)
public class TSalesforceConnectDefinition extends ComponentDefinition {

    public static final String COMPONENT_NAME = "tSalesforceConnect"; //$NON-NLS-1$

    protected ComponentConnector[] connectors = { new ComponentConnector(Type.FLOW, 0, 0),
            new ComponentConnector(Type.ITERATE, 1, 0), new ComponentConnector(Type.SUBJOB_OK, 1, 0),
            new ComponentConnector(Type.SUBJOB_ERROR, 1, 0) };

    protected SalesforceConnectionProperties properties;

    @Override
    public ComponentProperties createProperties() {
        return new TSalesforceConnectProperties();
    }

    @Override
    public Family[] getSupportedFamilies() {
        return new Family[] { Family.BUSINESS, Family.CLOUD };
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

}
