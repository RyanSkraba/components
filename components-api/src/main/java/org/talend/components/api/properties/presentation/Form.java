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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.components.api.NamedThing;
import org.talend.components.api.SimpleNamedThing;
import org.talend.components.api.ToStringIndent;
import org.talend.components.api.ToStringIndentUtil;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;

/**
 * Represents a collection of components {@link SchemaElement} objects that are grouped into a form for display. This
 * form can be manifested for example as a tab in a view, a dialog, or a page in a wizard.
 */
public class Form extends SimpleNamedThing implements ToStringIndent {

    /**
     * Standard form name for the main form associated with a component.
     *
     * This has no significance in the Component Framework, it's just a usage convention.
     */
    public static final String MAIN = "Main";

    /**
     * Standard form name for advanced properties associated with a component.
     *
     * This has no significance in the Component Framework, it's just a usage convention.
     */
    public static final String ADVANCED = "Advanced";

    /**
     * Standard form name for a form that references something (like a schema or other component), to be included in
     * some other form.
     *
     * This has no significance in the Component Framework, it's just a usage convention.
     */
    public static final String REFERENCE = "Reference";

    protected String subtitle;

    protected ComponentProperties properties;

    protected Map<String, NamedThing> children;

    protected Map<String, Widget> widgetMap;

    protected List<Widget> widgets;

    private int currentRow;

    private int currentColumn;

    private boolean callBeforeFormPresent;

    private boolean callAfterFormBack;

    private boolean callAfterFormNext;

    private boolean callAfterFormFinish;

    /**
     * Indicate that some {@link Widget} objects for this form have changed and the UI should be re-rendered to reflect
     * the changed widget.
     */
    protected boolean refreshUI;

    public Form() {
    }

    public Form(ComponentProperties props, String name, String displayName, String title) {
        super(name, displayName, title);
        children = new HashMap<String, NamedThing>();
        widgetMap = new HashMap<String, Widget>();
        widgets = new ArrayList<Widget>();
        props.addForm(this);
        properties = props;
        props.setFormLayoutMethods(name, this);
    }

    public static Form create(ComponentProperties props, String name, String title) {
        return new Form(props, name, name, title);
    }

    public List<NamedThing> getChildren() {
        List<NamedThing> l = new ArrayList<>();
        l.addAll(children.values());
        return l;
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public NamedThing getChild(Class<?> cls) {
        return getChild(cls.getSimpleName());
    }

    public NamedThing getChild(String name) {
        return children.get(name);
    }

    public ComponentProperties getProperties() {
        return properties;
    }

    public void setProperties(ComponentProperties properties) {
        this.properties = properties;
    }

    public Form setName(String name) {
        this.name = name;
        return this;
    }

    public Form setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Form setTitle(String title) {
        this.title = title;
        return this;
    }

    public Form setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public String getSubtitle() {
        return subtitle;
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
            String name = child.getName();
            /*
             * We don't use the form name since that's not going to necessarily be unique within the form's list of
             * properties. The ComponentProperties object associated with the form will have a unique name within the
             * enclosing ComponentProperties (and therefore this Form).
             */
            if (child instanceof Form) {
                name = ((Form) child).getProperties().getName();
            }
            if (name == null)
                throw new NullPointerException();
            widgetMap.put(name, widget);
            children.put(name, child);
            properties.setWidgetLayoutMethods(name, widget);
        }
    }

    public Widget getWidget(String child) {
        return widgetMap.get(child);
    }

    /**
     * Uses the class name to get the {@link Widget}. This is used in the case of references to
     * {@link ComponentProperties} which are by default named for their class.
     * 
     * @param childClass the Class of the desired {@link ComponentProperties} to get.
     * @return the {@code Widget} belonging to those properties.
     */
    public Widget getWidget(Class<?> childClass) {
        return widgetMap.get(childClass.getSimpleName());
    }

    public boolean isRefreshUI() {
        return refreshUI;
    }

    public void setRefreshUI(boolean refreshUI) {
        this.refreshUI = refreshUI;
    }

    public boolean isCallBeforeFormPresent() {
        return callBeforeFormPresent;
    }

    public void setCallBeforeFormPresent(boolean callBeforeFormPresent) {
        this.callBeforeFormPresent = callBeforeFormPresent;
    }

    public boolean isCallAfterFormBack() {
        return callAfterFormBack;
    }

    public void setCallAfterFormBack(boolean callAfterFormBack) {
        this.callAfterFormBack = callAfterFormBack;
    }

    public boolean isCallAfterFormNext() {
        return callAfterFormNext;
    }

    public void setCallAfterFormNext(boolean callAfterFormNext) {
        this.callAfterFormNext = callAfterFormNext;
    }

    public boolean isCallAfterFormFinish() {
        return callAfterFormFinish;
    }

    public void setCallAfterFormFinish(boolean callAfterFormFinish) {
        this.callAfterFormFinish = callAfterFormFinish;
    }

    public String toString() {
        return toStringIndent(0);
    }

    public String toStringIndent(int indent) {
        StringBuilder sb = new StringBuilder();
        String is = ToStringIndentUtil.indentString(indent);
        sb.append(is + "Form: " + getName());
        for (Widget w : getWidgets()) {
            sb.append("\n" + w.toStringIndent(indent + 2));
        }
        return sb.toString();
    }

}
