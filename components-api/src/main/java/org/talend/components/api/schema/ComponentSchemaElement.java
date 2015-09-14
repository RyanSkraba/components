package org.talend.components.api.schema;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface ComponentSchemaElement {

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
        DYNAMIC
    }

    public String getName();

    public void setName(String name);

    public String getDescription();

    public void setDescription(String description);

    public Type getType();

    public void setType(Type type);

    public int getSize();

    public void setSize(int size);

    public int getPrecision();

    public void setPrecision(int precision);

    public String getPattern();

    public void setPattern(String pattern);

    public String getDefaultValue();

    public void setDefaultValue(String defaultValue);

    public boolean isNullable();

    public void setNullable(boolean nullable);

    public List<ComponentSchemaElement> getChildren();

    public void setChildren(List<ComponentSchemaElement> children);

    public void addChild(ComponentSchemaElement child);

    public Map<String, ComponentSchemaElement> getChildMap();


}
