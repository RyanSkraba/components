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

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

import static org.talend.components.api.schema.SchemaFactory.newSchemaElement;

public class TSalesforceInputProperties extends ComponentProperties {

    public enum QueryMode {
        QUERY,
        BULK
    }

    public SchemaElement queryMode = newSchemaElement(SchemaElement.Type.ENUM, "QueryMode", "Query Mode");

    public SchemaElement condition = newSchemaElement("Condition", "Condition");

    public SchemaElement manualQuery = newSchemaElement(SchemaElement.Type.BOOLEAN, "ManualQuery", "Manual Query");

    public SchemaElement query = newSchemaElement("Query", "Full OSQL query string");

    public SchemaElement includeDeleted = newSchemaElement(SchemaElement.Type.BOOLEAN, "IncludeDeleted",
            "Include deleted records");

    //
    // Advanced
    //
    public SchemaElement batchSize = newSchemaElement(SchemaElement.Type.INT, "BatchSize", "Batch Size");

    public SchemaElement normalizeDelimiter = newSchemaElement("NormalizeDelimiter", "Normalize Delimeter");

    public SchemaElement columnNameDelimiter = newSchemaElement("ColumnNameDelimiter", "Column Name Delimiter");

    //
    // Collections
    //
    public SalesforceConnectionProperties connection = new SalesforceConnectionProperties();

    public SalesforceModuleProperties module = new SalesforceModuleProperties(connection);

    public TSalesforceInputProperties() {
        setupLayout();
    }

    public static final String MAIN = "Main";

    public static final String ADVANCED = "Advanced";

    @Override
    public void setupLayout() {
        Form mainForm = Form.create(this, MAIN, "Salesforce Input");
        mainForm.addRow(connection.getForm(SalesforceConnectionProperties.MAIN));
        mainForm.addRow(module.getForm(SalesforceModuleProperties.REFERENCE));
        mainForm.addRow(queryMode);
        mainForm.addRow(condition);
        mainForm.addRow(manualQuery);
        mainForm.addRow(query);
        mainForm.addRow(includeDeleted);
        refreshLayout(mainForm);

        Form advancedForm = Form.create(this, ADVANCED, "Salesforce Advanced");
        advancedForm.addRow(batchSize);
        advancedForm.addRow(normalizeDelimiter);
        advancedForm.addRow(columnNameDelimiter);

    }

    public void afterQueryMode() {
        refreshLayout(getForm(MAIN));
    }

    public void afterManualQuery() {
        refreshLayout(getForm(MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        form.getWidget(includeDeleted.getName()).setVisible(getValue(queryMode) == QueryMode.QUERY);

        form.getWidget(query.getName()).setVisible(getBooleanValue(manualQuery));
        form.getWidget(condition.getName()).setVisible(!getBooleanValue(manualQuery));
    }

}
