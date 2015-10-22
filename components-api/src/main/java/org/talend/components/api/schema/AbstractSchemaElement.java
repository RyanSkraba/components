package org.talend.components.api.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.components.api.SimpleNamedThing;

/**
 * This implementation shall be used to represent meta data elements
 */

public abstract class AbstractSchemaElement extends SimpleNamedThing implements SchemaElement {

    private static final String I18N_DISPLAY_NAME_SUFFIX = ".displayName"; //$NON-NLS-1$

    private static final String I18N_PROPERTY_PREFIX = "property."; //$NON-NLS-1$

    private Type type;

    private int size;

    private int occurMinTimes;

    private int occurMaxTimes;

    // Number of decimal places - DI
    private int precision;

    // Used for date conversion - DI
    private String pattern;

    private String defaultValue;

    private boolean nullable;

    private Class<?> enumClass;

    private List<?> possibleValues;

    protected List<SchemaElement> children;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SchemaElement setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * the displayName is returning the current name because for real data schema the display name never gets
     * translated.
     */
    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getI18nMessage(I18N_PROPERTY_PREFIX + name + I18N_DISPLAY_NAME_SUFFIX);
    }

    public SchemaElement setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public SchemaElement setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public SchemaElement setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public SchemaElement setSize(int size) {
        this.size = size;
        return this;
    }

    @Override
    public boolean isSizeUnbounded() {
        if (size == -1) {
            return true;
        }
        return false;
    }

    @Override
    public int getOccurMinTimes() {
        return occurMinTimes;
    }

    @Override
    public SchemaElement setOccurMinTimes(int times) {
        this.occurMinTimes = times;
        return this;
    }

    @Override
    public int getOccurMaxTimes() {
        return occurMaxTimes;
    }

    @Override
    public SchemaElement setOccurMaxTimes(int times) {
        this.occurMaxTimes = times;
        return this;
    }

    @Override
    public boolean isRequired() {
        return occurMinTimes > 0;
    }

    @Override
    public SchemaElement setRequired() {
        return setRequired(true);
    }

    @Override
    public SchemaElement setRequired(boolean required) {
        setOccurMinTimes(1);
        setOccurMaxTimes(1);
        return this;
    }

    @Override
    public int getPrecision() {
        return precision;
    }

    @Override
    public SchemaElement setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public SchemaElement setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public SchemaElement setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public SchemaElement setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    @Override
    public Class<?> getEnumClass() {
        return enumClass;
    }

    @Override
    public SchemaElement setEnumClass(Class<?> enumClass) {
        this.enumClass = enumClass;
        return this;
    }

    @Override
    public List<?> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public SchemaElement setPossibleValues(List<?> possibleValues) {
        this.possibleValues = possibleValues;
        return this;
    }

    @Override
    public SchemaElement setPossibleValues(Object... values) {
        this.possibleValues = Arrays.asList(values);
        return this;
    }

    @Override
    public List<SchemaElement> getChildren() {
        return children;
    }

    @Override
    public SchemaElement setChildren(List<SchemaElement> children) {
        this.children = children;
        return this;
    }

    @Override
    public SchemaElement addChild(SchemaElement child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return this;
    }

    @Override
    public SchemaElement getChild(String name) {
        if (children != null) {
            for (SchemaElement child : children) {
                if (child.getName().equals(name)) {
                    return child;
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, SchemaElement> getChildMap() {
        Map<String, SchemaElement> map = new HashMap<>();
        for (SchemaElement se : getChildren()) {
            map.put(se.getName(), se);
        }
        return map;
    }

}
