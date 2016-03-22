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

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.StudioConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.HasSchemaProperty;
import org.talend.components.common.SchemaProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.Property.Type;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

import java.util.Arrays;
import java.util.List;

import static org.talend.daikon.properties.PropertyFactory.newProperty;
import static org.talend.daikon.properties.presentation.Widget.widget;

public class TSalesforceOutputBulkProperties extends ComponentProperties implements HasSchemaProperty {

    public Property fileName = newProperty("fileName"); //$NON-NLS-1$

    public Property append = newProperty(Type.BOOLEAN, "append"); //$NON-NLS-1$

    public Property ignoreNull = newProperty(Type.BOOLEAN, "ignoreNull"); //$NON-NLS-1$

    public Property upsertRelation = (Property) newProperty("upsertRelation").setOccurMaxTimes(-1); //$NON-NLS-1$

    //
    // Collections
    //

    public SchemaProperties schema = new SchemaProperties("schema");

    public TSalesforceOutputBulkProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        TSalesforceOutputProperties.setupUpsertRelation(upsertRelation, TSalesforceOutputProperties.POLY);
        schema.schema.setTaggedValue(StudioConstants.CONNECTOR_TYPE_SCHEMA_KEY, ConnectorType.FLOW);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        mainForm.addRow(fileName);
        mainForm.addRow(append);
        mainForm.addRow(ignoreNull);

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(widget(upsertRelation).setWidgetType(Widget.WidgetType.TABLE));

    }

    @Override
    public List<Schema> getSchemas() {
        return Arrays.asList(new Schema[]{new Schema.Parser().parse(schema.schema.getStringValue())});
    }

    @Override
    public void setSchemas(List<Schema> schemas) {
        schema.schema.setValue(schemas.get(0));
    }

}
