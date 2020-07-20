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

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

public class SnowflakeDbTypeProperties extends ComponentPropertiesImpl  {

    private static final long serialVersionUID = 1L;

    private enum SNOWFLAKE_DBTYPE {
        ARRAY,
        BIGINT,
        BINARY,
        BOOLEAN,
        CHARACTER,
        DATE,
        DATETIME,
        DECIMAL,
        DOUBLE,
        DOUBLE_PRECISION,
        FLOAT,
        FLOAT4,
        FLOAT8,
        INTEGER,
        NUMBER,
        NUMERIC,
        OBJECT,
        REAL,
        SMALLINT,
        STRING,
        TEXT,
        TIME,
        TIMESTAMP,
        TIMESTAMP_LTZ,
        TIMESTAMP_NTZ,
        TIMESTAMP_TZ,
        VARBINARY,
        VARCHAR,
        VARIANT
    }

    private static final List<String> dbTypePossibleValues = new ArrayList<>();
    static {
        for(SNOWFLAKE_DBTYPE e : SNOWFLAKE_DBTYPE.values()){
            dbTypePossibleValues.add(e.name());
        }
    }

    public static final TypeLiteral<List<String>> LIST_STRING_TYPE = new TypeLiteral<List<String>>() {};

    public Property<List<String>> column = newProperty(LIST_STRING_TYPE, "column");
    public Property<List<String>> dbtype = newProperty(LIST_STRING_TYPE, "dbtype");

    private List<String> columnPossibleValues = new ArrayList<>();

    public SnowflakeDbTypeProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        setFieldNames(Collections.EMPTY_LIST);

        // to avoid setting of an exact first value in closed lists as a default value
        // for more info TDI-41286 or\and ComponentsUtils#getParameterValue
        column.setValue(Collections.emptyList());
        dbtype.setValue(Collections.emptyList());
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addColumn(Widget.widget(column).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(dbtype).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
    }



    public void setFieldNames(List<String> names) {
        this.columnPossibleValues.clear();
        if(names != null){
            this.columnPossibleValues.addAll(names);
        }
        this.updatePossibleValues();
    }

    private void updatePossibleValues(){
        this.column.setPossibleValues(columnPossibleValues);
        if(columnPossibleValues.isEmpty()){
            dbtype.setValue(null);
        }

        if(this.dbtype.getPossibleValues() == null || this.dbtype.getPossibleValues().isEmpty()){
            this.dbtype.setPossibleValues(dbTypePossibleValues);
        }
    }

}
