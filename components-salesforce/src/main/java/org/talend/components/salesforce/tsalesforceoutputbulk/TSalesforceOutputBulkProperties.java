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

import static org.talend.components.api.properties.presentation.Widget.widget;
import static org.talend.components.api.schema.SchemaFactory.newProperty;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

public class TSalesforceOutputBulkProperties extends ComponentProperties {

    public SchemaElement fileName = newProperty("fileName"); //$NON-NLS-1$

    public SchemaElement append = newProperty(SchemaElement.Type.BOOLEAN, "append"); //$NON-NLS-1$

    public SchemaElement ignoreNull = newProperty(SchemaElement.Type.BOOLEAN, "ignoreNull"); //$NON-NLS-1$

    public SchemaElement upsertRelation = newProperty("upsertRelation").setOccurMaxTimes(-1); //$NON-NLS-1$

    //
    // Collections
    //

    public SchemaProperties schema = new SchemaProperties().init();

    @Override
    public ComponentProperties init() {
        TSalesforceOutputProperties.setupUpsertRelation(upsertRelation);
        super.init();
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN, "Salesforce Output Bulk");
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        mainForm.addRow(fileName);
        mainForm.addRow(append);
        mainForm.addRow(ignoreNull);

        Form advancedForm = Form.create(this, Form.ADVANCED, "Advanced");
        advancedForm.addRow(widget(upsertRelation).setWidgetType(Widget.WidgetType.TABLE));

    }

}
