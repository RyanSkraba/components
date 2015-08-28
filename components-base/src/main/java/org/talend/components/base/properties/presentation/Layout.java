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
package org.talend.components.base.properties.presentation;

/**
 * The {@code Layout} class defines the presentation characteristics of the property within
 * its {@link Form}.
 * <p>
 * TODO - I think the name layout is not quite right here, this encompasses all declarative
 * property-specific presentation layoutMap.
 */
public class Layout {

    public enum WidgetType {
        DEFAULT, TEXT_FIELD, TEXT_AREA, LIST, COMBOBOX, RADIOBUTTONS, LISTBOX, BUTTON
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
     * The type of widget to be used to express this property. This is used only if there is
     * a choice given the type of property.
     */
    private WidgetType widgetType = WidgetType.DEFAULT;

    /**
     * Is the validation associated with this expected to be long running (so that the UI
     * should give a wait indication. This is for things like doing a connection or loading
     * data from a database.
     * <p>
     * TODO - perhaps in the future we can have some notion of progress.
     */
    private boolean longRunning;

    /**
     * This property is to be deemphasized in the UI. For example, it can be right-justified (in a LtoR UI)
     * to keep the description out of the column of the descriptions of the other layoutMap that might be
     * in a column.
     */
    private boolean deemphasize;

    public static Layout create() {
        return new Layout();
    }

    public int getRow() {
        return this.row;
    }

    public Layout setRow(int row) {
        this.row = row;
        return this;
    }

    public int getOrder() {
        return this.order;
    }

    public Layout setOrder(int order) {
        this.order = order;
        return this;
    }

    public Layout setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public WidgetType getWidgetType() {
        return widgetType;
    }

    public Layout setWidgetType(WidgetType widgetType) {
        this.widgetType = widgetType;
        return this;
    }

    public boolean isLongRunning() {
        return longRunning;
    }

    public Layout setLongRunning(boolean longRunning) {
        this.longRunning = longRunning;
        return this;
    }

    public boolean isDeemphasize() {
        return deemphasize;
    }

    public Layout setDeemphasize(boolean deemphasize) {
        this.deemphasize = deemphasize;
        return this;
    }
}
