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
package org.talend.component.properties;

import org.talend.component.properties.presentation.Layout;

import net.jodah.typetools.TypeResolver;

import java.util.List;

public class Property<T> {

    private String name;

    private String displayName;

    // TODO - what about properties that have an array of values (like selected tables)
    private T value = null;

    private List<T> possibleValues;

    private Layout layout;

    private ValidationResult validationResult;

    private boolean required;

    private boolean requestRefreshLayoutOnChange;

    // private Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public Property() {
        // this.persistentClass = (Class<T>) ((ParameterizedType)
        // getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Property(String name, String displayName) {
        this();
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public Property<T> setLayout(Layout layout) {
        this.layout = layout;
        return this;
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

    public String getTypeName() {
        return TypeResolver.resolveRawArgument(Property.class, getClass()).getTypeName();
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    /**
     * This will tell the client to refresh the Layout whenever the property changes
     */
    public void setRequestRefreshLayoutOnChange(boolean requestRefreshLayoutOnChange) {
        this.requestRefreshLayoutOnChange = requestRefreshLayoutOnChange;
    }

    public boolean getRequesRefreshOnChange() {
        return requestRefreshLayoutOnChange;
    }

}
