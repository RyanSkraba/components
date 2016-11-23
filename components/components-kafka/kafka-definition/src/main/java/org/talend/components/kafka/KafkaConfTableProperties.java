// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.kafka;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class KafkaConfTableProperties extends PropertiesImpl {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {// empty
    };

    public Property<List<String>> keyCol = PropertyFactory.newProperty(LIST_STRING_TYPE, "keyCol");

    public Property<List<String>> valueCol = PropertyFactory.newProperty(LIST_STRING_TYPE, "valueCol");

    public KafkaConfTableProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        // mainForm.addRow(Widget.widget(keyCol).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        mainForm.addRow(keyCol);
        mainForm.addColumn(valueCol);

    }

    // //FIXME can be done by front end with feature(new widget type)?
    // public void beforeKeyCol() {
    // refreshLayout(getForm(Form.MAIN));
    // }
    //
    // @Override
    // public void refreshLayout(Form form) {
    // super.refreshLayout(form);
    // if (Form.MAIN.equals(form.getName())) {
    // keyCol.setPossibleValues(keyCol.getPossibleValues().removeAll(keyCol.getValue()));
    // }
    // }
}
