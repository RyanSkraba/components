package org.talend.component.salesforce;

import org.springframework.stereotype.Service;
import org.talend.component.ComponentConnector;
import org.talend.component.ComponentConnector.Type;
import org.talend.component.ComponentDesign;
import org.talend.component.ComponentProperties;

@Service
public class SalesforceDesign extends ComponentDesign {

	protected ComponentConnector[] connectors = {
			new ComponentConnector(Type.FLOW, 0, 0),
			new ComponentConnector(Type.ITERATE, 1, 0),
			new ComponentConnector(Type.SUBJOB_OK, 1, 0),
			new ComponentConnector(Type.SUBJOB_ERROR, 1, 0) };

	protected SalesforceProperties properties;

	@Override
	public ComponentProperties createProperties() {
		return new SalesforceProperties();
	}

	@Override
	public Family[] getSupportedFamilies() {
		return new Family[] { Family.BUSINESS, Family.CLOUD };
	}

}
