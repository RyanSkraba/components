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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.Constants;
import org.talend.components.api.properties.AbstractComponentDefinition;
import org.talend.components.api.properties.ComponentConnector;
import org.talend.components.api.properties.ComponentConnector.Type;
import org.talend.components.api.properties.ComponentDefinition;
import org.talend.components.api.properties.ComponentImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceRuntime;

@org.springframework.stereotype.Component(Constants.COMPONENT_BEAN_PREFIX + TSalesforceConnectDefinition.COMPONENT_NAME)
@aQute.bnd.annotation.component.Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceConnectDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceConnectDefinition extends AbstractComponentDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(TSalesforceConnectDefinition.class);

    public static final String COMPONENT_NAME = "tSalesforceConnect"; //$NON-NLS-1$

    public TSalesforceConnectDefinition() {
        setConnectors(new ComponentConnector(Type.FLOW, 0, 0), new ComponentConnector(Type.ITERATE, 1, 0),
                new ComponentConnector(Type.SUBJOB_OK, 1, 0), new ComponentConnector(Type.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public ComponentProperties createProperties() {
        return new SalesforceConnectionProperties(null);
    }

    @Override
    public ComponentRuntime createRuntime() {
        return new SalesforceRuntime();
    }

    @Override
    public String[] getSupportedFamilies() {
        return new String[] { ComponentDefinition.FAMILY_BUSINESS, ComponentDefinition.FAMILY_CLOUD };
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return "tSalesforceConnect_icon32.png"; //$NON-NLS-1$

        default:
            // will return null
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "tSalesforceConnect";
    }

}
