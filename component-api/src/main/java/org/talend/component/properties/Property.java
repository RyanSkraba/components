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

import org.talend.component.properties.layout.Layout;

import net.jodah.typetools.TypeResolver;

/**
 * created by sgandon on 12 août 2015 Detailled comment
 *
 */
public class Property<T> {

    private String name;

    private T value;

    private Layout layout;

    // private Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public Property() {
        // this.persistentClass = (Class<T>) ((ParameterizedType)
        // getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Getter for layout.
     * 
     * @return the layout
     */
    public Layout getLayout() {
        return this.layout;
    }

    /**
     * Sets the layout.
     * 
     * @param layout the layout to set
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    /**
     * Getter for required.
     * 
     * @return the required
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Sets the required.
     * 
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    private boolean required;

    private String displayName;

    private boolean requestRefreshLayoutOnChange;

    /**
     * Getter for displayName.
     * 
     * @return the displayName
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Sets the displayName.
     * 
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Property(String name, String displayName, T defaultValue, boolean required) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.value = defaultValue;
        this.required = required;
    }

    /**
     * Getter for value.
     * 
     * @return the value
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     * 
     * @param value the value to set
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Getter for name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return TypeResolver.resolveRawArgument(Property.class, getClass()).getTypeName();
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
