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
package org.talend.components.salesforce.tsalesforceoutputbulk;

import static org.talend.daikon.properties.PropertyFactory.newProperty;
import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;

import org.talend.components.api.properties.ComponentPropertyFactory;
import org.talend.components.common.BulkFileProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class TSalesforceOutputBulkProperties extends BulkFileProperties {

    public Property ignoreNull = newProperty(Property.Type.BOOLEAN, "ignoreNull");

    public Property upsertRelation = newProperty("upsertRelation").setOccurMaxTimes(Property.INFINITE); //$NON-NLS-1$

    public TSalesforceOutputBulkProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        
        returns = ComponentPropertyFactory.newReturnsProperty();
        ComponentPropertyFactory.newReturnProperty(returns, Property.Type.INT, "NB_LINE"); //$NON-NLS-1$
        
        setupUpsertRelation(upsertRelation, TSalesforceOutputProperties.POLY);
        // schema.schema.setTaggedValue(StudioConstants.CONNECTOR_TYPE_SCHEMA_KEY, ConnectorType.FLOW);
    }
    
    private void setupUpsertRelation(Property ur, boolean poly) {
        ur.setChildren(new ArrayList<Property>());
        ur.addChild(newProperty("columnName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldName")); //$NON-NLS-1$
        ur.addChild(newProperty("lookupFieldModuleName")); //$NON-NLS-1$
        if (poly) {
            Property property = newProperty(Property.Type.BOOLEAN, "polymorphic");
            property.setValue(false);
            ur.addChild(property); //$NON-NLS-1$
        }
        ur.addChild(newProperty("lookupFieldExternalIdName")); //$NON-NLS-1$
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(ignoreNull);

        Form refForm = new Form(this, Form.REFERENCE);
        refForm.addRow(append);
        refForm.addRow(ignoreNull);

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(widget(upsertRelation).setWidgetType(Widget.WidgetType.TABLE));
    }

}
