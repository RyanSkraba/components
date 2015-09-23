package org.talend.components.api.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.talend.components.api.NamedThing;

/**
 *
 */
public interface SchemaElement extends NamedThing {

    public enum Type {
        STRING,
        BOOLEAN,
        INT,
        DATE,
        DATETIME,
        DECIMAL,
        FLOAT,
        DOUBLE,
        BYTE_ARRAY,
        ENUM,
        DYNAMIC,
        GROUP,
        SCHEMA
    }

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

    public SchemaElement setRequired(boolean required);

    public int getPrecision();

    public SchemaElement setPrecision(int precision);

    public String getPattern();

    public SchemaElement setPattern(String pattern);

    public String getDefaultValue();

    public SchemaElement setDefaultValue(String defaultValue);

    public boolean isNullable();

    public SchemaElement setNullable(boolean nullable);

    public Class getEnumClass();

    public SchemaElement setEnumClass(Class enumClass);

    public List<?> getPossibleValues();

    public SchemaElement setPossibleValues(List<?> possibleValues);

    public List<SchemaElement> getChildren();

    public SchemaElement setChildren(List<SchemaElement> children);

    public SchemaElement addChild(SchemaElement child);

    public Map<String, SchemaElement> getChildMap();

}
