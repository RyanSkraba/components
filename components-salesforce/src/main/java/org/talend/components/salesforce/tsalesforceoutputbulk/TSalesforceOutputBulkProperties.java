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
import org.talend.components.salesforce.UpsertRelationTable;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class TSalesforceOutputBulkProperties extends BulkFileProperties {

    public Property ignoreNull = newProperty(Property.Type.BOOLEAN, "ignoreNull");

    public UpsertRelationTable upsertRelationTable = new UpsertRelationTable("upsertRelationTable");

    public TSalesforceOutputBulkProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        
        returns = ComponentPropertyFactory.newReturnsProperty();
        upsertRelationTable.setUsePolymorphic(true);
        ComponentPropertyFactory.newReturnProperty(returns, Property.Type.INT, "NB_LINE"); //$NON-NLS-1$        
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
        advancedForm.addRow(widget(upsertRelationTable).setWidgetType(Widget.WidgetType.TABLE));
    }

}
