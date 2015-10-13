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

import java.util.List;
import java.util.Map;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentConnector;
import org.talend.components.api.component.ComponentConnector.Type;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.api.runtime.ComponentRuntimeContainer;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceRuntime;

import com.sforce.soap.partner.GetDeletedResult;

@org.springframework.stereotype.Component(Constants.COMPONENT_BEAN_PREFIX + TSalesforceGetDeletedDefinition.COMPONENT_NAME)
@aQute.bnd.annotation.component.Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceGetDeletedDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceGetDeletedDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceGetDeleted"; //$NON-NLS-1$

    public TSalesforceGetDeletedDefinition() {
        super(COMPONENT_NAME);
        propertiesClass = TSalesforceGetDeletedProperties.class;
        setConnectors(new ComponentConnector(Type.FLOW, 1, 1), new ComponentConnector(Type.SUBJOB_OK, 1, 0),
                new ComponentConnector(Type.SUBJOB_ERROR, 1, 0));
    }

    @Override public ComponentRuntime createRuntime() {
        return new SalesforceRuntime() {

            @Override public void inputBegin(ComponentProperties props, ComponentRuntimeContainer container, List<Map<String, Object>> values) throws Exception {

                TSalesforceGetDeletedProperties gdProps = (TSalesforceGetDeletedProperties) props;
                String module = gdProps.module.getStringValue(gdProps.module.moduleName);

                GetDeletedResult result = getDeleted(module, gdProps.getCalendarValue(gdProps.startDate), gdProps.getCalendarValue(gdProps.endDate));

                // FIXME - finish this
            }

            @Override public void inputEnd(ComponentProperties props, ComponentRuntimeContainer container, List<Map<String, Object>> values)
                    throws Exception {

            }

        };
    }

    public boolean isStartable() {
        return true;
    }

    public String getPartitioning() {
        return AUTO;
    }

}
