package org.talend.component.salesforce.tsalesforceconnect;

import org.springframework.stereotype.Component;
import org.talend.component.ComponentConnector;
import org.talend.component.ComponentConnector.Type;
import org.talend.component.ComponentDefinition;
import org.talend.component.ComponentProperties;
import org.talend.component.Constants;
import org.talend.component.salesforce.SalesforceConnectionProperties;

@Component(Constants.COMPONENT_BEAN_PREFIX + TSalesforceConnectDefinition.COMPONENT_NAME)
public class TSalesforceConnectDefinition extends ComponentDefinition {

    public static final String COMPONENT_NAME = "SalesforceConnect"; //$NON-NLS-1$

    protected ComponentConnector[] connectors = { new ComponentConnector(Type.FLOW, 0, 0),
            new ComponentConnector(Type.ITERATE, 1, 0), new ComponentConnector(Type.SUBJOB_OK, 1, 0),
            new ComponentConnector(Type.SUBJOB_ERROR, 1, 0) };

    protected SalesforceConnectionProperties properties;

    @Override
    public ComponentProperties createProperties() {
        return new SalesforceConnectionProperties();
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
