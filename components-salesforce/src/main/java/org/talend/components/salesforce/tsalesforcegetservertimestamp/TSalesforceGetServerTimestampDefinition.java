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
package org.talend.components.salesforce.tsalesforcegetservertimestamp;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceRuntime;
import org.talend.daikon.schema.Schema;

import aQute.bnd.annotation.component.Component;

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceGetServerTimestampDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceGetServerTimestampDefinition extends SalesforceDefinition {

    public static final String COMPONENT_NAME = "tSalesforceGetServerTimestampNew"; //$NON-NLS-1$

    public TSalesforceGetServerTimestampDefinition() {
        super(COMPONENT_NAME);

        setConnectors(new Connector(ConnectorType.FLOW, 0, 1));
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 0), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public ComponentRuntime createRuntime() {
        return new SalesforceRuntime() {

            Calendar result;

            Schema schema;

            @Override
            public void inputBegin(ComponentProperties props) throws Exception {
                TSalesforceGetServerTimestampProperties gdProps = (TSalesforceGetServerTimestampProperties) props;
                connect(gdProps.connection);
                schema = (Schema) gdProps.schema.schema.getValue();
                result = connection.getServerTimestamp().getTimestamp();
            }

            @Override
            public Map<String, Object> inputRow() throws Exception {
                if (result == null) {
                    return null;
                }

                Map<String, Object> map = new HashMap<>();
                // FIXME - error checking - what if there are no columns
                map.put(schema.getRoot().getChildren().get(0).getName(), result);
                result = null;
                return map;
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
    public Class<?> getPropertyClass() {
        return TSalesforceGetServerTimestampProperties.class;
    }

}
