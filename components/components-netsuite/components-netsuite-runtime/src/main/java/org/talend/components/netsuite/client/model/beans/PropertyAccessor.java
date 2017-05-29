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

/**
 * Used to access properties for classes generated from NetSuite's XML schemas.
 */
public interface PropertyAccessor<T> {

    /**
     * Get value of a property.
     *
     * @param target target object
     * @param name name of property
     * @return value
     */
    Object get(T target, String name);

    /**
     * Set value of a property.
     *
     * @param target target object
     * @param name name of property
     * @param value value to be set
     */
    void set(T target, String name, Object value);
}
