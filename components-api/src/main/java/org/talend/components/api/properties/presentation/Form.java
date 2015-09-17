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

import org.talend.components.api.AbstractNamedThing;
import org.talend.components.api.NamedThing;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;

import java.util.*;

/**
 * Represents a collection of components {@link SchemaElement} objects that are grouped into a form for display. This
 * form can be manifested for example as a tab in a view, a dialog, or a page in a wizard.
 */
public class Form extends AbstractNamedThing {

    protected ComponentProperties properties;

    protected Map<String, NamedThing> children;

    protected Map<String, Widget> widgetMap;

    protected List<Widget> widgets;

    private int currentRow;

    private int currentColumn;

    /**
     * Indicate that some {@link Widget} objects for this form have changed and the UI should be re-rendered to reflect
     * the changed widget.
     */
    protected boolean refreshUI;

    public Form(ComponentProperties props, String name, String displayName, String title) {
        super(name, displayName, title);
        children = new HashMap<String, NamedThing>();
        widgetMap = new HashMap<String, Widget>();
        widgets = new ArrayList<Widget>();
        props.addForm(this);
        properties = props;
    }

    public static Form create(ComponentProperties props, String name, String title) {
        return new Form(props, props.setupFormName(name), name, title);
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
    public List<Widget> getWidgets() {
        return widgets;
    }

    public Form addRow(NamedThing child) {
        addRow(Widget.widget(child));
        return this;
    }

    public Form addColumn(NamedThing child) {
        addColumn(Widget.widget(child));
        return this;
    }

    /**
     * Add the widget in the next row and first column, it's relative, only forward and step always is 1.
     * 
     * @param widget
     * @return
     */
    public Form addRow(Widget widget) {
        currentColumn = 1;
        widgets.add(widget.setRow(++currentRow).setOrder(currentColumn));
        fill(widget);
        return this;
    }

    /**
     * Add the widget in the next column of current row, it's relative, only forward and step always is 1.
     * 
     * @param widget
     * @return
     */
    public Form addColumn(Widget widget) {
        widgets.add(widget.setRow(currentRow).setOrder(++currentColumn));
        fill(widget);
        return this;
    }

    private void fill(Widget widget) {
        for (NamedThing child : widget.getProperties()) {
            widgetMap.put(child.getName(), widget);
            children.put(child.getName(), child);
            properties.setLayoutMethods(child.getName(), widget);
        }
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
