// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.data;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.ComponentConstants;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionDefinition;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;


public class MarkLogicDatasetProperties extends ComponentPropertiesImpl implements DatasetProperties<MarkLogicConnectionProperties> {

    public ReferenceProperties<MarkLogicConnectionProperties> datastore = new ReferenceProperties<>("datastore", MarkLogicConnectionDefinition.COMPONENT_NAME);

    public Property<String> criteria = PropertyFactory.newString("criteria");
    public Property<Integer> pageSize = PropertyFactory.newInteger("pageSize");
    public Property<Boolean> useQueryOption = PropertyFactory.newBoolean("useQueryOption");
    public Property<String> queryLiteralType = PropertyFactory.newString("queryLiteralType");
    public Property<String> queryOptionName = PropertyFactory.newString("queryOptionName");
    public Property<String> queryOptionLiterals = PropertyFactory.newString("queryOptionLiterals");

    public SchemaProperties main = new SchemaProperties("main");

    public MarkLogicDatasetProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        criteria.setRequired();
        criteria.setValue("");
        useQueryOption.setValue(false);
        pageSize.setValue(10);
        queryLiteralType.setPossibleValues("XML", "JSON");
        queryLiteralType.setValue("XML");
        queryOptionLiterals.setTaggedValue(ComponentConstants.LINE_SEPARATOR_REPLACED_TO, " ");
     }

    public void setupSchema() {
        Schema.Field docIdField = new Schema.Field("docId", AvroUtils._string(), null, (Object) null,
                Schema.Field.Order.ASCENDING);
        docIdField.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        Schema.Field newDocContentField = new Schema.Field("docContent", AvroUtils._string(), null, (Object) null,
                Schema.Field.Order.IGNORE);
        List<Schema.Field> fields = new ArrayList<>();
        fields.add(docIdField);
        fields.add(newDocContentField);
        Schema initialSchema = Schema.createRecord("markLogic", null, null, false, fields);
        main.schema.setValue(initialSchema);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form referenceForm = Form.create(this, Form.REFERENCE);
        referenceForm.addRow(main.getForm(Form.REFERENCE));
        referenceForm.setVisible(false);

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(criteria);

        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(pageSize);
        advancedForm.addRow(useQueryOption);
        advancedForm.addRow(widget(queryLiteralType).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        advancedForm.addColumn(queryOptionName);
        advancedForm.addRow(widget(queryOptionLiterals).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(queryLiteralType).setVisible(useQueryOption.getValue());
            form.getWidget(queryOptionName).setVisible(useQueryOption.getValue());
            form.getWidget(queryOptionLiterals).setVisible(useQueryOption.getValue());
        }
    }

    public void afterUseQueryOption() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public MarkLogicConnectionProperties getDatastoreProperties() {
        return datastore.getReference();
    }

    @Override
    public void setDatastoreProperties(MarkLogicConnectionProperties datastoreProperties) {
        datastore.setReference(datastoreProperties);
    }

}
