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

import static org.talend.daikon.properties.PropertyFactory.*;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;

public class TSalesforceOutputProperties extends SalesforceOutputProperties {

    //
    // Advanced
    //
    public Property extendInsert = newBoolean("extendInsert"); //$NON-NLS-1$

    public Property ceaseForError = newBoolean("ceaseForError"); //$NON-NLS-1$

    public Property ignoreNull = newBoolean("ignoreNull"); //$NON-NLS-1$

    public Property retrieveInsertId = newBoolean("retrieveInsertId"); //$NON-NLS-1$

    public Property commitLevel = newInteger("commitLevel"); //$NON-NLS-1$

    // FIXME - should be file
    public Property logFileName = newString("logFileName"); //$NON-NLS-1$


    public TSalesforceOutputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        commitLevel.setValue(200);
        ceaseForError.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(extendInsert);
        advancedForm.addRow(ceaseForError);
        advancedForm.addRow(ignoreNull);
        advancedForm.addRow(retrieveInsertId);
        advancedForm.addRow(commitLevel);
        advancedForm.addRow(logFileName);
    }

    public void afterExtendInsert() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterRetrieveInsertId() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.ADVANCED)) {
            Schema s = SchemaBuilder.record("Main").fields().endRecord();
            schemaFlow.schema.setValue(s);
            if (!extendInsert.getBooleanValue() && retrieveInsertId.getBooleanValue()
                    && ACTION_INSERT.equals(outputAction.getValue())) {
                s = SchemaBuilder.record("Main").fields().name("salesforce_id").type().
                        stringType().noDefault().endRecord();
            }
            form.getWidget("commitLevel").setVisible(extendInsert.getBooleanValue());
            form.getWidget("retrieveInsertId").setVisible(extendInsert.getBooleanValue() && ACTION_INSERT.equals(outputAction.getValue()));
            form.getWidget("ignoreNull").setVisible(ACTION_UPDATE.equals(outputAction.getValue())||ACTION_UPSERT.equals(outputAction.getValue()));

        }
    }

}
