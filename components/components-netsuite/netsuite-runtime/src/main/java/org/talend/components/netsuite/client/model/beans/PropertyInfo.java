// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite.client.model.beans;

import java.lang.reflect.Method;

/**
 *
 */
public class PropertyInfo {
    private String name;
    private Class<?> readType;
    private Class<?> writeType;
    private String readMethodName;
    private String writeMethodName;

    public PropertyInfo(String name, Class<?> readType, Class<?> writeType,
            Method readMethod, Method writeMethod) {
        this(name, readType, writeType,
                readMethod != null ? readMethod.getName() : null,
                writeMethod != null ? writeMethod.getName() : null);
    }

    public PropertyInfo(String name, Class<?> readType, Class<?> writeType,
            String readMethodName, String writeMethodName) {
        this.name = name;
        this.readType = readType;
        this.writeType = writeType;
        this.readMethodName = readMethodName;
        this.writeMethodName = writeMethodName;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReadType() {
        return readType;
    }

    public Class<?> getWriteType() {
        return writeType;
    }

    public String getReadMethodName() {
        return readMethodName;
    }

    public String getWriteMethodName() {
        return writeMethodName;
    }
}
