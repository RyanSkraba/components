package org.talend.components.common;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;

public class ValuesTrimPropertis extends ComponentPropertiesImpl {

    public Property<Boolean> trimAll = newBoolean("trimAll");

    public TrimFieldsTable trimTable = new TrimFieldsTable("trimTable");

    private List<String> fieldNames = new ArrayList<>();

    public ValuesTrimPropertis(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(trimAll);
        mainForm.addRow(widget(trimTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (Form.MAIN.equals(form.getName())) {
            form.getWidget(trimTable.getName()).setHidden(trimAll.getValue());
        }
    }

    public void beforeTrimTable() {
        if (fieldNames != null && fieldNames.size() > 0) {
            trimTable.columnName.setValue(fieldNames);
            List<Boolean> trimValues = new ArrayList<>();
            for (int i = fieldNames.size(); i > 0; i--) {
                trimValues.add(Boolean.FALSE);
            }
            trimTable.trim.setValue(trimValues);
        }
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }
}
