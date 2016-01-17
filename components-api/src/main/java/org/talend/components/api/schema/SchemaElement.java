// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.schema;

import org.talend.components.api.NamedThing;
import org.talend.components.api.ToStringIndent;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface SchemaElement extends NamedThing, ToStringIndent {

    public enum Type {
        STRING,
        BOOLEAN,
        INT,
        LONG,
        DATE,
        DATETIME,
        DECIMAL,
        FLOAT,
        DOUBLE,
        BYTE_ARRAY,
        ENUM,
        DYNAMIC,
        GROUP,
        SCHEMA,
        OBJECT,
        CHARACTER,
        LIST,
        SHORT,
        BYTE,
    }

    public static int INFINITE = -1;

    @Override
    public String getName();

    public SchemaElement setName(String name);

    @Override
    public String getTitle();

    public SchemaElement setTitle(String description);

    public Type getType();

    public SchemaElement setType(Type type);

    public int getSize();

    public SchemaElement setSize(int size);

    public int getOccurMinTimes();

    public boolean isSizeUnbounded();

    public SchemaElement setOccurMinTimes(int times);

    public int getOccurMaxTimes();

    public SchemaElement setOccurMaxTimes(int times);

    public boolean isRequired();

    public SchemaElement setRequired();

    public SchemaElement setRequired(boolean required);

    public int getPrecision();

    public SchemaElement setPrecision(int precision);

    public String getPattern();

    public SchemaElement setPattern(String pattern);

    public String getDefaultValue();

    public SchemaElement setDefaultValue(String defaultValue);

    public boolean isNullable();

    public SchemaElement setNullable(boolean nullable);

    public Class<?> getEnumClass();

    public SchemaElement setEnumClass(Class<?> enumClass);

    public List<?> getPossibleValues();

    public SchemaElement setPossibleValues(List<?> possibleValues);

    public SchemaElement setPossibleValues(Object... values);

    public List<SchemaElement> getChildren();

    public SchemaElement getChild(String name);

    public SchemaElement setChildren(List<SchemaElement> children);

    public SchemaElement addChild(SchemaElement child);

    public Map<String, SchemaElement> getChildMap();

}
