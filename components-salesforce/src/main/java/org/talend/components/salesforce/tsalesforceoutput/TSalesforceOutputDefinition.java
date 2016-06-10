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
package org.talend.components.salesforce.tsalesforceoutput;

import aQute.bnd.annotation.component.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.runtime.SalesforceSink;

import aQute.bnd.annotation.component.Component;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

/**
 * Component that can connect to a salesforce system and put some data into it.
 */

@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSalesforceOutputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSalesforceOutputDefinition extends SalesforceDefinition implements OutputComponentDefinition {

    public static final String COMPONENT_NAME = "tSalesforceOutput"; //$NON-NLS-1$

    public TSalesforceOutputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return false;
    }

    @Override
    public boolean isConditionalInputs() {
        return true;
    }

    @Override
    public String getPartitioning() {
        return AUTO;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSalesforceOutputProperties.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return concatPropertiesClasses(super.getNestedCompatibleComponentPropertiesClass(),
                new Class[] { SalesforceModuleProperties.class });
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[]{newProperty(RETURN_TOTAL_RECORD_COUNT), newProperty(RETURN_SUCCESS_RECORD_COUNT),
                newProperty(RETURN_REJECT_RECORD_COUNT)};
    }

    @Override
    public Sink getRuntime() {
        return new SalesforceSink();
    }

}
