// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite.input;

import static org.talend.components.netsuite.util.ComponentExceptions.exceptionToValidationResult;
import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.netsuite.NetSuiteModuleProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.components.netsuite.schema.SearchFieldInfo;
import org.talend.components.netsuite.schema.SearchInfo;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 *
 */
public class NetSuiteInputModuleProperties extends NetSuiteModuleProperties {

    public final SearchQueryProperties searchQuery;

    public NetSuiteInputModuleProperties(String name, NetSuiteConnectionProperties connectionProperties) {
        super(name, connectionProperties);

        searchQuery = new SearchQueryProperties("searchQuery");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(widget(moduleName)
                .setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE)
                .setLongRunning(true));
        mainForm.addRow(main.getForm(Form.REFERENCE));
        mainForm.addRow(widget(searchQuery)
                .setWidgetType(Widget.TABLE_WIDGET_TYPE));

        Form refForm = Form.create(this, Form.REFERENCE);
        refForm.addRow(widget(moduleName)
                .setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE)
                .setLongRunning(true));
        refForm.addRow(main.getForm(Form.REFERENCE));
        refForm.addRow(widget(searchQuery)
                .setWidgetType(Widget.TABLE_WIDGET_TYPE));

    }

    public ValidationResult beforeModuleName() throws Exception {
        try {
            List<NamedThing> searchableTypes = getSearchableTypes();
            moduleName.setPossibleNamedThingValues(searchableTypes);

            return ValidationResult.OK;
        } catch (Exception ex) {
            return exceptionToValidationResult(ex);
        }
    }

    public ValidationResult afterModuleName() throws Exception {
        try {
            setupSchema();
            setupSearchSchema();
            refreshLayout(getForm(Form.MAIN));
            return ValidationResult.OK;

        } catch (Exception ex) {
            return exceptionToValidationResult(ex);
        }
    }

    public void afterSearchQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    protected void setupSchema() {
        Schema schema = getSchema(moduleName.getStringValue());
        main.schema.setValue(schema);
    }

    protected void setupSearchSchema() {
        SearchInfo searchSchema = getSearchInfo(moduleName.getValue());
        List<String> fieldNames = new ArrayList<>(searchSchema.getFields().size());
        for (SearchFieldInfo field : searchSchema.getFields()) {
            fieldNames.add(field.getName());
        }

        searchQuery.field.setPossibleValues(fieldNames);
        searchQuery.field.setValue(new ArrayList<String>());

        searchQuery.operator.setPossibleValues(getSearchFieldOperators());
        searchQuery.operator.setValue(new ArrayList<String>());

        searchQuery.value1.setValue(new ArrayList<String>());
        searchQuery.value2.setValue(new ArrayList<String>());

        searchQuery.refreshLayout(searchQuery.getForm(Form.MAIN));
    }
}
