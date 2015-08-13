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

/**
 * created by sgandon on 12 août 2015 Detailled comment
 *
 */
public class Layout {

    private int row = 0;

    private String group = null;

    private int order = 0;

    private boolean visible = true;

    /**
     * Getter for row.
     * 
     * @return the row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Sets the row.
     * 
     * @param row the row to set
     * @return
     */
    public Layout setRow(int row) {
        this.row = row;
        return this;
    }

    /**
     * Getter for group.
     * 
     * @return the group
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Sets the group.
     * 
     * @param group the group to set
     * @return
     */
    public Layout setGroup(String group) {
        this.group = group;
        return this;
    }

    /**
     * Getter for order.
     * 
     * @return the order
     */
    public int getOrder() {
        return this.order;
    }

    /**
     * Sets the order.
     * 
     * @param order the order to set
     */
    public Layout setOrder(int order) {
        this.order = order;
        return this;
    }

    public static Layout create() {
        return new Layout();
    }

    /**
     * specify if the associated component should be displayed or not
     * 
     * @param b
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

}
