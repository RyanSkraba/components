package org.talend.components.processing.definition.aggregate;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class AggregateGroupByProperties extends PropertiesImpl {

    public AggregateGroupByProperties(String name) {
        super(name);
    }

    /**
     * This enum will be filled with the name of the input field.
     */
    public Property<String> fieldPath = PropertyFactory.newString("fieldPath", "").setRequired();

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(fieldPath).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
    }
}
