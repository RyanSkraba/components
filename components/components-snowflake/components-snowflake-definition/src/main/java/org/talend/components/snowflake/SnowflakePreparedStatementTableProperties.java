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
package org.talend.components.snowflake;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowDefinition;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 * This class is responsible for inserting proper values into prepared statement used in {@link TSnowflakeRowDefinition} component<br/>
 * It contains: <b>Indexes</b>, <b>Types</b>, <b>Values</b>.<br/>
 * Field <b>Indexes</b> is responsible for specifying a correct index in prepared statement.<br/>
 * <b>Type</b> is responsible for database type used for inserting value in prepared statement.<br/>
 * <b>Values</b> actual values to be inserted into prepared statement.
 *
 */
public class SnowflakePreparedStatementTableProperties extends ComponentPropertiesImpl {

    private static final long serialVersionUID = 6572118926633324186L;

    private static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {
    };

    private static final TypeLiteral<List<Integer>> LIST_INTEGER_TYPE = new TypeLiteral<List<Integer>>() {
    };

    private static final TypeLiteral<List<Object>> LIST_OBJECT_TYPE = new TypeLiteral<List<Object>>() {
    };

    public SnowflakePreparedStatementTableProperties(String name) {
        super(name);
    }

    public Property<List<Integer>> indexes = newProperty(LIST_INTEGER_TYPE, "indexes");

    public Property<List<String>> types = newProperty(LIST_STRING_TYPE, "types");

    public Property<List<Object>> values = newProperty(LIST_OBJECT_TYPE, "values");

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(indexes);
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