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
package org.talend.components.salesforce.tsalesforceconnection;

import aQute.bnd.annotation.component.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.EndpointComponentDefinition;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceConnectionDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceConnectionDefinition extends SalesforceDefinition implements EndpointComponentDefinition {

    public static final String COMPONENT_NAME = "tSalesforceConnection"; //$NON-NLS-1$

    public TSalesforceConnectionDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return SalesforceConnectionProperties.class;
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public SourceOrSink getRuntime() {
        return new SalesforceSourceOrSink();
    }
}
