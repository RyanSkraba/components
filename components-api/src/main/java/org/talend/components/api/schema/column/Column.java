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
package org.talend.components.api.schema.column;

import org.talend.components.api.schema.column.type.common.ExternalBaseType;
import org.talend.components.api.schema.column.type.common.TBaseType;
import org.talend.components.api.schema.column.type.common.TypeMapping;

import java.util.ArrayList;
import java.util.List;


public class Column {

    private String col_name;

    private String app_col_name;

    private Class<? extends TBaseType> col_type;

    private Class<? extends ExternalBaseType> app_col_type;

    private String appFamily;

    private boolean buildFromT;

    public Column(boolean buildFromT, String name, Class type, String appFamily) {
        this.buildFromT = buildFromT;
        if (buildFromT) {
            this.col_name = name;
            this.col_type = type;
        } else {
            this.app_col_name = name;
            this.app_col_type = type;
        }
        this.appFamily = appFamily;
    }

    public List<Class<? extends TBaseType>> getOptionalTalendTypes() {
        if (buildFromT) {
            List<Class<? extends TBaseType>> types = new ArrayList<Class<? extends TBaseType>>();
            types.add(col_type);
            return types;
        }
        return TypeMapping.getTalendTypes(appFamily, app_col_type);
    }

    public List<Class<? extends ExternalBaseType>> getOptionalAppTypes(String appFamily) {
        if (!buildFromT) {
            List<Class<? extends ExternalBaseType>> types = new ArrayList<Class<? extends ExternalBaseType>>();
            types.add(app_col_type);
            return types;
        }
        return TypeMapping.getAppTypes(appFamily, col_type);
    }

    public void setTalendType(String col_name, Class<? extends TBaseType> col_type) {
        if (!buildFromT) {
            // check on config stage
            if (!TypeMapping.getTalendTypes(appFamily, app_col_type).contains(col_type)) {
                throw new RuntimeException("unsupport set talend type " + col_type + " for " + appFamily + " type " + app_col_type);
            }
            this.col_name = col_name;
            this.col_type = col_type;
        }
    }

    public void setAppType(String appFamily, String app_col_name, Class<? extends ExternalBaseType> app_col_type) {
        if (buildFromT) {
            if (!TypeMapping.getAppTypes(appFamily, col_type).contains(app_col_type)) {
                throw new RuntimeException("unsupport set " + appFamily + " type " + app_col_type + " for talend type" + col_type);
            }
            this.app_col_name = app_col_name;
            this.app_col_type = app_col_type;
        }
    }

    /**
     * Getter for col_name.
     *
     * @return the col_name
     */
    public String getCol_name() {
        return this.col_name;
    }

    /**
     * Getter for app_col_name.
     *
     * @return the app_col_name
     */
    public String getApp_col_name() {
        return this.app_col_name;
    }

    /**
     * Getter for app_col_type.
     *
     * @return the app_col_type
     */
    public Class<? extends ExternalBaseType> getApp_col_type() {
        return this.app_col_type;
    }

    /**
     * Getter for col_type.
     *
     * @return the col_type
     */
    public Class<? extends TBaseType> getCol_type() {
        return this.col_type;
    }
}
