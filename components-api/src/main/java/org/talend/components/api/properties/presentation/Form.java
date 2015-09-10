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

import java.util.*;

import org.talend.components.api.AbstractNamedThing;
import org.talend.components.api.NamedThing;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;

/**
 * Represents a collection of components {@link Property} objects that are grouped into a form for display. This form
 * can be manifested for example as a tab in a view, a dialog, or a page in a wizard.
 */
public class Form extends AbstractNamedThing {

    protected ComponentProperties             properties;

    protected Map<String, AbstractNamedThing> children;

    protected Map<String, Widget>             widgetMap;

    protected List<Widget>                    widgets;

    /**
     * Indicate that some {@link Widget} objects for this form have changed and the UI should be re-rendered to reflect
     * the changed widget.
     */
    protected boolean                         refreshUI;

    public Form(ComponentProperties props, String name, String displayName) {
        super(name, displayName);
        children = new HashMap<String, AbstractNamedThing>();
        widgetMap = new HashMap<String, Widget>();
        widgets = new ArrayList<Widget>();
        props.addForm(this);
        properties = props;
    }

    public static Form create(ComponentProperties props, String name, String displayName) {
        return new Form(props, name, displayName);
    }

    public Collection<AbstractNamedThing> getChildren() {
        return children.values();
    }

    public NamedThing getChild(String name) {
        return children.get(name);
    }

    public ComponentProperties getProperties() {
        return properties;
    }

    // FIXME - only here for JSON
    public List<Widget> getWidgets() {
        return widgets;
    }

    public Form addChild(AbstractNamedThing child) {
        addChild(Widget.widget(child));
        return this;
    }

    public Form addChild(Widget widget) {
        widgets.add(widget);
        for (AbstractNamedThing child : widget.getProperties()) {
            widgetMap.put(child.getName(), widget);
            children.put(child.getName(), child);
            properties.setLayoutMethods(child.getName(), widget);
        }
        return this;
    }

    public Widget getWidget(String child) {
        return widgetMap.get(child);
    }

    public boolean isRefreshUI() {
        return refreshUI;
    }

    public void setRefreshUI(boolean refreshUI) {
        this.refreshUI = refreshUI;
    }
}
