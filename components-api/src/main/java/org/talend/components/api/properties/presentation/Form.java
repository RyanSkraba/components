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
package org.talend.components.api.properties.presentation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.ComponentProperties;
import org.talend.components.api.properties.NamedThing;
import org.talend.components.api.properties.Property;

/**
 * Represents a collection of components {@link Property} objects that are grouped into a form for display. This form
 * can be manifested for example as a tab in a view, a dialog, or a page in a wizard.
 */
public class Form extends NamedThing {

    protected ComponentProperties     properties;

    protected Map<String, NamedThing> children;

    protected Map<String, Layout>     layoutMap;

    /**
     * Indicate that some {@link Layout} objects for this form have changed and the UI should be re-rendered to reflect
     * the changed layout.
     */
    protected boolean                 refreshUI;

    public Form(ComponentProperties props, String name, String displayName) {
        super(name, displayName);
        children = new HashMap<String, NamedThing>();
        layoutMap = new HashMap<String, Layout>();
        props.addForm(this);
        properties = props;
    }

    public static Form create(ComponentProperties props, String name, String displayName) {
        return new Form(props, name, displayName);
    }

    public Collection<NamedThing> getChildren() {
        return children.values();
    }

    public NamedThing getChild(String name) {
        return children.get(name);
    }

    public ComponentProperties getProperties() {
        return properties;
    }

    // FIXME - only here for JSON
    public Map<String, Layout> getLayoutMap() {
        return layoutMap;
    }

    public Form addChild(NamedThing child, Layout layout) {
        if (child == null)
            throw new NullPointerException();
        layoutMap.put(child.getName(), layout);
        children.put(child.getName(), child);
        properties.setLayoutMethods(child.getName(), layout);
        return this;
    }

    public Layout getLayout(String child) {
        return layoutMap.get(child);
    }

    public boolean isRefreshUI() {
        return refreshUI;
    }

    public void setRefreshUI(boolean refreshUI) {
        this.refreshUI = refreshUI;
    }
}
