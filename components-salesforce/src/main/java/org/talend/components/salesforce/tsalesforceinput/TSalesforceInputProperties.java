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

import static org.talend.components.api.schema.SchemaFactory.newProperty;

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;

public class TSalesforceInputProperties extends ComponentProperties {

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

    //
    // Collections
    //
    public SalesforceConnectionProperties connection;

    public SalesforceModuleProperties module;

    public TSalesforceInputProperties(I18nMessageProvider i18nMessageProvider) {
        super(i18nMessageProvider, "org.talend.components.salesforce.message"); //$NON-NLS-1$
        connection = new SalesforceConnectionProperties(i18nMessageProvider);

        module = new SalesforceModuleProperties(i18nMessageProvider, connection);
        setupLayout();
        setupPropertiesWithI18n();
    }

    public static final String MAIN = "Main"; //$NON-NLS-1$

    public static final String ADVANCED = "Advanced"; //$NON-NLS-1$

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
