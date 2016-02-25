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

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.SalesforceUserPasswordProperties;

import aQute.bnd.annotation.component.Component;

/**
 * Component that can connect to a salesforce system and get some data out of it.
 */

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceInputDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceInputNew"; //$NON-NLS-1$

    public TSalesforceInputDefinition() {
        super(COMPONENT_NAME);

        setConnectors(new Connector(ConnectorType.FLOW, 0, 1));
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 1), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSalesforceInputProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { SchemaProperties.class, SalesforceModuleProperties.class, SalesforceConnectionProperties.class,
                OauthProperties.class, SalesforceUserPasswordProperties.class, ProxyProperties.class,
                UserPasswordProperties.class };
    }

}
