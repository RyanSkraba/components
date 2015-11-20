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

import org.talend.components.api.NamedThing;
import org.talend.components.api.ToStringIndent;
import org.talend.components.api.ToStringIndentUtil;
import org.talend.components.api.schema.SchemaElement;

import java.util.Collection;

/**
 * The {@code Widget} class defines the presentation characteristics of the property within its {@link Form}.
 */
public class Widget implements ToStringIndent {

    public enum WidgetType {
                            /**
                             * No special widget is requested, the default for the property's type is to be used.
                             */
        DEFAULT,

                            /**
                             * Presentation of a schema editor.
                             */
        SCHEMA_EDITOR,

                            /**
                             * Presentation of a reference to a schema on one line. This shows the name of the schema
                             * and provides a button to open the schema editor/viewer in a dialog.
                             */
        SCHEMA_REFERENCE,

                            /**
                             * Provides a means of selecting a name or name/description from a set of names, possibly
                             * arranged in a hierarchy. This is to be used for a large number of names, as this has
                             * search capability.
                             *
                             * The NAME_SELECTION_AREA will operate on a property whose occur max times is -1, and whose
                             * value is a {@code List<NameAndLabel>}. It will show everything on the list and then once
                             * complete will set the values of the list only to those that are selected.
                             */
        NAME_SELECTION_AREA,

                            /**
                             * A reference to a named selection. This just shows the selected name and a button to get a
                             * dialog that has the {@link #NAME_SELECTION_AREA}.
                             */
        NAME_SELECTION_REFERENCE,

                            /**
                             * A reference to a component. This could be a reference to this component, another single
                             * component in the enclosing scope's type, or a specified component instance. This is
                             * rendered as a single line with the type of reference in a combo box.
                             */
        COMPONENT_REFERENCE,

                            /**
                             * A button
                             */
        BUTTON,

                            /*
                             * A table, the children of this {@link SchemaElement} will be the columns for the table.
                             * The maximum occurrence value of this {@code SchemaElement} is the number of possible rows
                             * in the table.
                             */
                            TABLE

    }

    /**
     * The row in the form where this property is to be presented. Starting with 1.
     */
    private int row;

    /**
     * The order in the row where this property is to be presented. Starting with 1.
     */
    private int order;

    private boolean visible = true;

    /**
     * The type of widget to be used to express this property. This is used only if there is a choice given the type of
     * property.
     */
    private WidgetType widgetType = WidgetType.DEFAULT;

    /**
     * Is the validation associated with this expected to be long running (so that the UI should give a wait indication.
     * This is for things like doing a connection or loading data from a database.
     */
    private boolean longRunning;

    /**
     * This property is to be deemphasized in the UI. For example, it can be right-justified (in a LtoR UI) to keep the
     * description out of the column of the descriptions of the other properties that might be in a column.
     */
    private boolean deemphasize;

    //
    // Internal properties set by the component framework
    //

    private boolean callBefore;

    private boolean callValidate;

    private boolean callAfter;

    /**
     * Used for the {@link WidgetType#COMPONENT_REFERENCE} to give the name of the components to search for in the
     * referenced environment, i.e. the job.
     */
    private String referencedComponentName;

    private NamedThing[] properties;

    public static Widget widget(NamedThing... properties) {
        return new Widget(properties);
    }

    public Widget(NamedThing... properties) {
        setProperties(properties);
    }

    public void setProperties(NamedThing... properties) {
        this.properties = properties;
    }

    public NamedThing[] getProperties() {
        return properties;
    }

    public int getRow() {
        return this.row;
    }

    public Widget setRow(int row) {
        this.row = row;
        return this;
    }

    public int getOrder() {
        return this.order;
    }

    public Widget setOrder(int order) {
        this.order = order;
        return this;
    }

    public Widget setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public WidgetType getWidgetType() {
        return widgetType;
    }

    public Widget setWidgetType(WidgetType widgetType) {
        this.widgetType = widgetType;
        return this;
    }

    public boolean isLongRunning() {
        return longRunning;
    }

    public Widget setLongRunning(boolean longRunning) {
        this.longRunning = longRunning;
        return this;
    }

    public boolean isDeemphasize() {
        return deemphasize;
    }

    public Widget setDeemphasize(boolean deemphasize) {
        this.deemphasize = deemphasize;
        return this;
    }

    public String getReferencedComponentName() {
        return referencedComponentName;
    }

    public void setReferencedComponentName(String referencedComponentName) {
        this.referencedComponentName = referencedComponentName;
    }

    //
    // These are automatically set by the component framework; they
    // are not to be specified by the user.
    //

    public boolean isCallBefore() {
        return callBefore;
    }

    public void setCallBefore(boolean callBefore) {
        this.callBefore = callBefore;
    }

    public boolean isCallValidate() {
        return callValidate;
    }

    public void setCallValidate(boolean callValidate) {
        this.callValidate = callValidate;
    }

    public boolean isCallAfter() {
        return callAfter;
    }

    public void setCallAfter(boolean callAfter) {
        this.callAfter = callAfter;
    }

    public String toString() {
        return toStringIndent(0);
    }

    public String toStringIndent(int indent) {
        StringBuilder sb = new StringBuilder();
        String is = ToStringIndentUtil.indentString(indent);
        sb.append(is + "Widget: " + getWidgetType() + " " + getRow() + "/" + getOrder() + " ");
        boolean firstTime = true;
        for (NamedThing n : getProperties()) {
            if (!firstTime)
                sb.append(", ");
            if (n instanceof Form)
                sb.append("Form: ");
            sb.append(n.getName());
            if (n instanceof Form)
                sb.append(" (" + ((Form) n).getProperties().getName() + ")");
            if (n instanceof SchemaElement) {
                Collection values = ((SchemaElement)n).getPossibleValues();
                if (values != null)
                    sb.append(" Values: " + values);
            }
            firstTime = false;
        }
        if (isCallBefore())
            sb.append(" CALL_BEFORE");
        if (isCallAfter())
            sb.append(" CALL_AFTER");
        if (isCallValidate())
            sb.append(" CALL_VALIDATE");
        return sb.toString();
    }

}
