package org.talend.components.api.schema.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.components.api.schema.ComponentSchemaElement;

/**
 */
public class ComponentSchemaElementImpl implements ComponentSchemaElement {

    public ComponentSchemaElementImpl() {
        setType(Type.STRING);
        setSize(-1);
    }

    public ComponentSchemaElementImpl(String name) {
        this();
        setName(name);
    }

    private String                         name;

    private String                         description;

    private Type                           type;

    private int                            size;

    // Number of decimal places - DI
    private int                            precision;

    // Used for date conversion - DI
    private String                         pattern;

    private String                         defaultValue;

    private boolean                        nullable;

    protected List<ComponentSchemaElement> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isSizeUnbounded() {
        if (size == -1)
            return true;
        return false;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public List<ComponentSchemaElement> getChildren() {
        return children;
    }

    public void setChildren(List<ComponentSchemaElement> children) {
        this.children = children;
    }

    public void addChild(ComponentSchemaElement child) {
        if (children == null)
            children = new ArrayList<ComponentSchemaElement>();
        children.add(child);
    }

    public Map<String, ComponentSchemaElement> getChildMap() {
        Map<String, ComponentSchemaElement> map = new HashMap();
        for (ComponentSchemaElement se : getChildren()) {
            map.put(se.getName(), se);
        }
        return map;
    }

}
