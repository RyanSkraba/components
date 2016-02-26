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
package org.talend.components.salesforce.tsalesforcegetdeleted;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceGetDeletedUpdatedProperties;
import org.talend.components.salesforce.SalesforceRuntime;
import org.talend.components.salesforce.SalesforceUserPasswordProperties;

import com.sforce.soap.partner.GetDeletedResult;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceGetDeletedDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceGetDeletedDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceGetDeletedNew"; //$NON-NLS-1$

    public TSalesforceGetDeletedDefinition() {
        super(COMPONENT_NAME);

        setConnectors(new Connector(ConnectorType.FLOW, 1, 1));
        setTriggers(new Trigger(TriggerType.SUBJOB_OK, 1, 0), new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public ComponentRuntime createRuntime() {
        return new SalesforceRuntime() {

            @Override
            public void inputBegin(ComponentProperties props) throws Exception {

                SalesforceGetDeletedUpdatedProperties gdProps = (SalesforceGetDeletedUpdatedProperties) props;
                String module = gdProps.module.moduleName.getStringValue();

                GetDeletedResult result = connection.getDeleted(module, gdProps.startDate.getCalendarValue(),
                        gdProps.endDate.getCalendarValue());

                // FIXME - finish this
            }

        };
    }

    @Override
    public boolean isStartable() {
        return true;
    }

    @Override
    public String getPartitioning() {
        return AUTO;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return SalesforceConnectionProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { OauthProperties.class, SalesforceUserPasswordProperties.class, ProxyProperties.class,
                UserPasswordProperties.class };
    }

}
