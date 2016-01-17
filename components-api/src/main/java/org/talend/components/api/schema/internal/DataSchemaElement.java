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
package org.talend.components.api.schema.internal;

import org.talend.components.api.schema.AbstractSchemaElement;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.ExternalBaseType;

/**
 * Represents meta data elements. This typically defines the DisplayName to be the same
 * value as Name because the technical name of the metadata schema is never translated
 */

public class DataSchemaElement extends AbstractSchemaElement {

    /**
     * the displayName is returning the current name because for real data schema the display name never gets
     * translated.
     */
    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public SchemaElement setDisplayName(String name) {
        setName(name);
        return this;
    }

    private String appColName;

    public String getAppColName() {
        return appColName;
    }

    public void setAppColName(String appColName) {
        this.appColName = appColName;
    }

    private Class<? extends ExternalBaseType> appColType;

    public Class<? extends ExternalBaseType> getAppColType() {
        return appColType;
    }

    public void setAppColType(Class<? extends ExternalBaseType> appColType) {
        this.appColType = appColType;
    }

    private boolean key;

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

}
