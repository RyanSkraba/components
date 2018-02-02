package org.talend.components.processing.definition.aggregate;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class AggregateOperationProperties extends PropertiesImpl {

    public AggregateOperationProperties(String name) {
        super(name);
    }

    /**
     * This enum will be filled with the path of the input field.
     */
    public Property<String> fieldPath = PropertyFactory.newString("fieldPath", "").setRequired();

    /**
     * This enum represent the operation applicable to the input value when grouping. The operations
     * displayed by the UI are dependent of the type of the field.
     *
     * If field's type is numerical (Integer, Long, Float or Double), Operation will contain "SUM, AVG, MIN, MAX"
     * and "COUNT, LIST"
     *
     * If field's type is not numerical, Operation will contain "COUNT, LIST" only
     *
     */
    public EnumProperty<AggregateFieldOperationType> operation =
            PropertyFactory.newEnum("operation", AggregateFieldOperationType.class);

    /**
     * This is the output field name defined by user, if empty, then the output field will be
     * "inputFieldName_operationName"
     */
    public Property<String> outputFieldPath = PropertyFactory.newString("outputFieldPath", "");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(fieldPath).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
        mainForm.addRow(operation);
        mainForm.addRow(outputFieldPath);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        operation.setValue(AggregateFieldOperationType.LIST);
    }

}
