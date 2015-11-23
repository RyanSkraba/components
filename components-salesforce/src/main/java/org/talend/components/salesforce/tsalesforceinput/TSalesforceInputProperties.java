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

import static org.talend.components.api.properties.PropertyFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;

public class TSalesforceInputProperties extends SalesforceConnectionModuleProperties {

    public static final String QUERY_QUERY = "Query";

    public static final String QUERY_BULK = "Bulk";

    public Property queryMode = newProperty(SchemaElement.Type.ENUM, "queryMode"); //$NON-NLS-1$

    public Property condition = newProperty("condition"); //$NON-NLS-1$

    public Property manualQuery = newProperty(SchemaElement.Type.BOOLEAN, "manualQuery"); //$NON-NLS-1$

    public Property query = newProperty("query"); //$NON-NLS-1$

    public Property includeDeleted = newProperty(SchemaElement.Type.BOOLEAN, "includeDeleted"); //$NON-NLS-1$

    //
    // Advanced
    //
    public Property batchSize = newProperty(SchemaElement.Type.INT, "batchSize"); //$NON-NLS-1$

    public Property normalizeDelimiter = newProperty("normalizeDelimiter"); //$NON-NLS-1$

    public Property columnNameDelimiter = newProperty("columnNameDelimiter"); //$NON-NLS-1$

    public TSalesforceInputProperties(String name) {
        super(name);
    }

    @Override
    public ComponentProperties init() {
        returns = setReturnsProperty();
        newReturnProperty(returns, SchemaElement.Type.INT, "NB_LINE");
        // FIXME - should use default value
        setValue(batchSize, 100);

        queryMode.setPossibleValues(QUERY_QUERY, QUERY_BULK);

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
            form.getWidget(includeDeleted.getName()).setVisible(
                    getValue(queryMode) != null && getValue(queryMode).equals(QUERY_QUERY));

            form.getWidget(query.getName()).setVisible(getBooleanValue(manualQuery));
            form.getWidget(condition.getName()).setVisible(!getBooleanValue(manualQuery));
        }
    }

}
