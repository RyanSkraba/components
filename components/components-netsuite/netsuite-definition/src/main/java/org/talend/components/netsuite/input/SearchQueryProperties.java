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

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 *
 */
public class SearchQueryProperties extends ComponentPropertiesImpl {

    /**
     *
     */
    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {};

    /**
     * Named constructor to be used is these properties are nested in other properties. Do not subclass this method for
     * initialization, use {@link #init()} instead.
     *
     * @param name
     */
    public SearchQueryProperties(String name) {
        super(name);
    }

    public final Property<List<String>> field = newProperty(LIST_STRING_TYPE, "field");

    public final Property<List<String>> operator = newProperty(LIST_STRING_TYPE, "operator");

    public final Property<List<String>> value1 = newProperty(LIST_STRING_TYPE, "value1");

    public final Property<List<String>> value2 = newProperty(LIST_STRING_TYPE, "value2");

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(widget(field)
                .setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(widget(operator)
                .setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(value1);
        mainForm.addColumn(value2);
    }

}
