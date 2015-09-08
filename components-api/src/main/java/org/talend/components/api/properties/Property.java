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
package org.talend.components.api.properties;

import java.util.List;

import net.jodah.typetools.TypeResolver;

public class Property<T> extends NamedThing {

    // TODO - what about properties that have an array of values (like selected tables)
    private T                value = null;

    private List<T>          possibleValues;

    private ValidationResult validationResult;

    private boolean          required;

    // private Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public Property() {
        // this.persistentClass = (Class<T>) ((ParameterizedType)
        // getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Property(String name, String displayName) {
        super(name, displayName);
    }

    public boolean isRequired() {
        return this.required;
    }

    public T getValue() {
        return this.value;
    }

    public Property<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public Property<T> setRequired(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * @return the canonical name of the type handled by this property instance.
     * */
    public String getTypeName() {
        // FIXME - this is not working.
        return TypeResolver.resolveRawArgument(Property.class, getClass()).getName();
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public List<T> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(List<T> possibleValues) {
        this.possibleValues = possibleValues;
    }
}
