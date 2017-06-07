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
package org.talend.components.azurestorage.table.tazurestorageinputtable;

import static org.talend.components.azurestorage.table.helpers.FilterExpressionTable.ADD_QUOTES;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.azurestorage.table.AzureStorageTableProperties;
import org.talend.components.azurestorage.table.helpers.FilterExpressionTable;
import org.talend.components.azurestorage.table.helpers.NameMappingTable;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TAzureStorageInputTableProperties extends AzureStorageTableProperties {

    private static final long serialVersionUID = -6167192340391830059L;

    public Property<Boolean> useFilterExpression = PropertyFactory.newBoolean("useFilterExpression");

    public FilterExpressionTable filterExpression = new FilterExpressionTable("filterExpression");

    public Property<String> producedFilter = newString("producedFilter");
    
    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");
    
    public NameMappingTable nameMapping = new NameMappingTable("nameMapping");

    public TAzureStorageInputTableProperties(String name) {
        super(name);

        setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateFilterExpressionTable();
            }
        });
    }

    @Override
    public Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        dieOnError.setValue(true);

        // default Input schema
        Schema s = SchemaBuilder.record("Main").fields()
                //
                .name("PartitionKey").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("RowKey").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_IS_LOCKED, "true").type(AvroUtils._string()).noDefault()
                //
                .name("Timestamp").prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd hh:mm:ss")// $NON-NLS-3$
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "20")// $NON-NLS-3$
                .type(AvroUtils._date()).noDefault()
                //
                .endRecord();
        schema.schema.setValue(s);
        //
        useFilterExpression.setValue(false);
        producedFilter.setValue("");
        producedFilter.setTaggedValue(ADD_QUOTES, true);

    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(useFilterExpression);
        mainForm.addRow(widget(filterExpression).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.getWidget(filterExpression.getName()).setVisible(false);
        mainForm.addRow(producedFilter);
        mainForm.getWidget(producedFilter.getName()).setVisible(false);
        mainForm.getWidget(producedFilter.getName()).setReadonly(true);
        //
        mainForm.addRow(dieOnError);
        
        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(widget(nameMapping).setWidgetType(Widget.TABLE_WIDGET_TYPE));

    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(filterExpression.getName()).setVisible(useFilterExpression.getValue());
            form.getWidget(producedFilter.getName()).setVisible(useFilterExpression.getValue());
            if (useFilterExpression.getValue()) {
                producedFilter.setValue(filterExpression.generateCombinedFilterConditions());
            }
        }
    }

    protected void updateFilterExpressionTable() {
        List<String> fieldNames = AvroUtils.getFieldNames(schema.schema.getValue());
        filterExpression.updateSchemaColumnNames(fieldNames);
    }

    public void afterUseFilterExpression() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterFilterExpression() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }
    
    public ValidationResult validateNameMapping() {
        return nameMapping.validateNameMappings();
    }

}
