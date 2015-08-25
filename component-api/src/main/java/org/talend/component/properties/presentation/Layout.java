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

/**
 * The {@code Layout} class defines the presentation characteristics of the property within
 * its {@link Form}.
 * <p>
 * TODO - I think the name layout is not quite right here, this encompasses all declarative
 * property-specific presentation properties.
 */
public class Layout {

    /**
     * The type of widget to be used to express this property.
     */
    public enum WidgetType {
        DEFAULT, TEXT_FIELD, LIST
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
     * Is the validation associated with this expected to be long running (so that the UI
     * should give a wait indication. This is for things like doing a connection or loading
     * data from a database.
     * <p>
     * TODO - perhaps in the future we can have some notion of progress.
     */
    private boolean longRunning;

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

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

    public boolean isLongRunning() {
        return longRunning;
    }

    public Layout setLongRunning(boolean longRunning) {
        this.longRunning = longRunning;
        return this;
    }
}
