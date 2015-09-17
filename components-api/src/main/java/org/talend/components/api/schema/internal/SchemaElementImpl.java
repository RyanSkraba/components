package org.talend.components.api.schema.internal;

import org.talend.components.api.schema.SchemaElement;

import java.util.*;

/**
 */
public class SchemaElementImpl implements SchemaElement {

    public SchemaElementImpl() {
        setType(Type.STRING);
        setSize(-1);
    }

    public SchemaElementImpl(String name) {
        this();
        setName(name);
    }

    public SchemaElementImpl(String name, String title) {
        this(name);
        setTitle(title);
    }

    public SchemaElementImpl(Type type, String name, String title) {
        this(name);
        setType(type);
        setTitle(title);
    }

    private String name;

    private String title;

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

    private Class enumClass;

    private Collection possibleValues;

    protected List<SchemaElement> children;

    public String getName() {
        return name;
    }

    public SchemaElement setName(String name) {
        this.name = name;
        return this;
    }

    public String getDisplayName() {
        return getName();
    }

    public SchemaElement setDisplayName(String name) {
        setName(name);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SchemaElement setTitle(String title) {
        this.title = title;
        return this;
    }

    public Type getType() {
        return type;
    }

    public SchemaElement setType(Type type) {
        this.type = type;
        return this;
    }

    public int getSize() {
        return size;
    }

    public SchemaElement setSize(int size) {
        this.size = size;
        return this;
    }

    public boolean isSizeUnbounded() {
        if (size == -1)
            return true;
        return false;
    }

    public int getOccurMinTimes() {
        return occurMinTimes;
    }

    public SchemaElement setOccurMinTimes(int times) {
        this.occurMinTimes = times;
        return this;
    }

    public int getOccurMaxTimes() {
        return occurMaxTimes;
    }

    public SchemaElement setOccurMaxTimes(int times) {
        this.occurMaxTimes = times;
        return this;
    }

    public boolean isRequired() {
        return occurMinTimes > 0;
    }

    public SchemaElement setRequired(boolean required) {
        setOccurMinTimes(1);
        setOccurMaxTimes(1);
        return this;
    }

    public int getPrecision() {
        return precision;
    }

    public SchemaElement setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public SchemaElement setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public SchemaElement setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public boolean isNullable() {
        return nullable;
    }

    public SchemaElement setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public Class getEnumClass() {
        return enumClass;
    }

    public SchemaElement setEnumClass(Class enumClass) {
        this.enumClass = enumClass;
        return this;
    }

    public Collection getPossibleValues() {
        return possibleValues;
    }

    public SchemaElement setPossibleValues(Collection possibleValues) {
        this.possibleValues = possibleValues;
        return this;
    }

    public List<SchemaElement> getChildren() {
        return children;
    }

    public SchemaElement setChildren(List<SchemaElement> children) {
        this.children = children;
        return this;
    }

    public SchemaElement addChild(SchemaElement child) {
        if (children == null)
            children = new ArrayList<SchemaElement>();
        children.add(child);
        return this;
    }

    public Map<String, SchemaElement> getChildMap() {
        Map<String, SchemaElement> map = new HashMap();
        for (SchemaElement se : getChildren()) {
            map.put(se.getName(), se);
        }
        return map;
    }

}
