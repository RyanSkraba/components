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
package org.talend.components.salesforce.tsalesforceinput;

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.salesforce.SalesforceInputOutputProperties;

public class TSalesforceInputProperties extends SalesforceInputOutputProperties {

    public enum QueryMode {
                           QUERY,
                           BULK
    }

    public SchemaElement queryMode = newProperty(SchemaElement.Type.ENUM, "QueryMode"); //$NON-NLS-1$

    public SchemaElement condition = newProperty("Condition"); //$NON-NLS-1$

    public SchemaElement manualQuery = newProperty(SchemaElement.Type.BOOLEAN, "ManualQuery"); //$NON-NLS-1$

    public SchemaElement query = newProperty("Query"); //$NON-NLS-1$

    public SchemaElement includeDeleted = newProperty(SchemaElement.Type.BOOLEAN, "IncludeDeleted"); //$NON-NLS-1$

    //
    // Advanced
    //
    public SchemaElement batchSize = newProperty(SchemaElement.Type.INT, "BatchSize"); //$NON-NLS-1$

    public SchemaElement normalizeDelimiter = newProperty("NormalizeDelimiter"); //$NON-NLS-1$

    public SchemaElement columnNameDelimiter = newProperty("ColumnNameDelimiter"); //$NON-NLS-1$

    @Override
    public ComponentProperties init() {
        returns = setReturnsProperty();
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_LINE");
        // FIXME - should use default value
        setValue(batchSize, 100);
        return super.init();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(queryMode);
        mainForm.addRow(condition);
        mainForm.addRow(manualQuery);
        mainForm.addRow(query);
        mainForm.addRow(includeDeleted);

        Form advancedForm = Form.create(this, Form.ADVANCED, "Salesforce Advanced");
        advancedForm.addRow(batchSize);
        advancedForm.addRow(normalizeDelimiter);
        advancedForm.addRow(columnNameDelimiter);
    }

    public void afterQueryMode() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterManualQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(includeDeleted.getName()).setVisible(getValue(queryMode) == QueryMode.QUERY);

            form.getWidget(query.getName()).setVisible(getBooleanValue(manualQuery));
            form.getWidget(condition.getName()).setVisible(!getBooleanValue(manualQuery));
        }
    }

}
