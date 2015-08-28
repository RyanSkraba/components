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
package org.talend.component.properties.presentation;

import java.util.HashMap;
import java.util.Map;

import org.talend.component.properties.Property;

/**
 * Represents a collection of components {@link org.talend.component.properties.Property} objects that are grouped into
 * a form for display. This form can be manifested for example as a tab in a view, a dialog, or a page in a wizard.
 */
public class Form {

    private String                   name;

    private String                   displayName;

    private Map<Property<?>, Layout> properties;

    public Form(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        properties = new HashMap<Property<?>, Layout>();
    }

    public static Form create(String name, String displayName) {
        return new Form(name, displayName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<Property<?>, Layout> getProperties() {
        return properties;
    }

    public void setProperties(Map<Property<?>, Layout> properties) {
        this.properties = properties;
    }

    public Form addProperty(Property<?> property, Layout layout) {
        properties.put(property, layout);
        return this;
    }

}
