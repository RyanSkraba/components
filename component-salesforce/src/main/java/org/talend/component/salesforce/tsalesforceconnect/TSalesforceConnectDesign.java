package org.talend.component.salesforce.tsalesforceconnect;

import org.springframework.stereotype.Service;
import org.talend.component.ComponentConnector;
import org.talend.component.ComponentConnector.Type;
import org.talend.component.ComponentDesign;
import org.talend.component.ComponentProperties;
import org.talend.component.salesforce.SalesforceConnectionProperties;

@Service
public class TSalesforceConnectDesign extends ComponentDesign {

	protected ComponentConnector[] connectors = {
			new ComponentConnector(Type.FLOW, 0, 0),
			new ComponentConnector(Type.ITERATE, 1, 0),
			new ComponentConnector(Type.SUBJOB_OK, 1, 0),
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

}
