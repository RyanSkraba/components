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

import java.util.Calendar;

import org.talend.components.api.schema.AbstractSchemaElement;

/**
 * A property that is part of a {@link ComponentProperties}.
 */
public class Property extends AbstractSchemaElement {

    protected ComponentProperties componentProperties;

    public Property(String name) {
        this(name, null);
    }

    public Property(String name, String title) {
        this(null, name, title);
    }

    public Property(Type type, String name, String title) {
        setName(name);
        setType(type == null ? Type.STRING : type);
        setTitle(title);
        setSize(-1);
    }

    public Property(Type type, String name) {
        this(type, name, null);
    }

    public void setValue(Object value) {
        componentProperties.setValue(this, value);
    }

    public Object getValue() {
        return componentProperties.getValue(this);
    }

    public boolean getBooleanValue() {
        return componentProperties.getBooleanValue(this);
    }

    public String getStringValue() {
        return componentProperties.getStringValue(this);
    }

    public int getIntValue() {
        return componentProperties.getIntValue(this);
    }

    public Calendar getCalendarValue() {
        return componentProperties.getCalendarValue(this);
    }

    public String toString() {
        return "Property: " + getName();
    }

    // Not API
    public void setComponentProperties(ComponentProperties props) {
        componentProperties = props;
    }

}
