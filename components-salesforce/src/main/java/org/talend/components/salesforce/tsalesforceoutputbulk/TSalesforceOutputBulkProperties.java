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

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.properties.ComponentPropertyFactory;
import org.talend.components.common.BulkFileProperties;
import org.talend.components.salesforce.UpsertRelationTable;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class TSalesforceOutputBulkProperties extends BulkFileProperties {

    public Property<Boolean> ignoreNull = newBoolean("ignoreNull");

    public UpsertRelationTable upsertRelationTable = new UpsertRelationTable("upsertRelationTable");

    public TSalesforceOutputBulkProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        returns = ComponentPropertyFactory.newReturnsProperty();
        upsertRelationTable.setUsePolymorphic(true);
        ComponentPropertyFactory.newReturnProperty(returns, newString("ERROR_MESSAGE")); //$NON-NLS-1$ 
        ComponentPropertyFactory.newReturnProperty(returns, newInteger("NB_LINE")); //$NON-NLS-1$
            
        this.setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
            	beforeUpsertRelationTable();
            }
            
        });
    }

    public void beforeUpsertRelationTable() {
        upsertRelationTable.columnName.setPossibleValues(getFieldNames(schema.schema));
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

    protected List<String> getFieldNames(Property schema) {
        String sJson = schema.getStringValue();
        Schema s = new Schema.Parser().parse(sJson);
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : s.getFields()) {
            fieldNames.add(f.name());
        }
        return fieldNames;
    }

}
