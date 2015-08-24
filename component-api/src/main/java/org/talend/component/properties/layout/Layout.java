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
package org.talend.component.properties.layout;

public class Layout {

    private int row;

    private String group;

    private int order;

    private boolean visible = true;

    public int getRow() {
        return this.row;
    }

    public Layout setRow(int row) {
        this.row = row;
        return this;
    }

    public String getGroup() {
        return this.group;
    }

    public Layout setGroup(String group) {
        this.group = group;
        return this;
    }

    public int getOrder() {
        return this.order;
    }

    public Layout setOrder(int order) {
        this.order = order;
        return this;
    }

    public static Layout create() {
        return new Layout();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

}
