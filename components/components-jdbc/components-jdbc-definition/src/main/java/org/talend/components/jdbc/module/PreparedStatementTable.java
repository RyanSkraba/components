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
package org.talend.components.jdbc.module;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 * common JDBC prepared statement table properties
 *
 */
// have to implement ComponentProperties, not good
public class PreparedStatementTable extends ComponentPropertiesImpl {

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    private static final TypeLiteral<List<Integer>> LIST_INTEGER_TYPE = new TypeLiteral<List<Integer>>() {
    };

    private static final TypeLiteral<List<Object>> LIST_OBJECT_TYPE = new TypeLiteral<List<Object>>() {
    };

    public PreparedStatementTable(String name) {
        super(name);
    }

    public Property<List<Integer>> indexs = newProperty(LIST_INTEGER_TYPE, "indexs");

    public Property<List<String>> types = newProperty(LIST_STRING_TYPE, "types");

    public Property<List<Object>> values = newProperty(LIST_OBJECT_TYPE, "values");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(indexs);
        mainForm.addColumn(new Widget(types).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(values);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        types.setPossibleValues(Arrays.asList(Type.values()));
    }

    public enum Type {
        BigDecimal,
        Blob,
        Boolean,
        Byte,
        Bytes,
        Clob,
        Date,
        Double,
        Float,
        Int,
        Long,
        Object,
        Short,
        String,
        Time,
        Null
    }

}
