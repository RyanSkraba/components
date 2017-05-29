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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptor of a bean.
 *
 * <p>This is simplified version of {@link java.beans.BeanInfo} and is intended
 * to be used for beans generated from NetSuite's XML schemas.
 */
public class BeanInfo {

    /** Properties of bean. */
    private List<PropertyInfo> properties;

    /** Table of properties by names, for faster access. */
    private Map<String, PropertyInfo> propertyMap;

    public BeanInfo(PropertyInfo[] properties) {
        this(Arrays.asList(properties));
    }

    public BeanInfo(List<PropertyInfo> properties) {
        this.properties = new ArrayList<>(properties);
        propertyMap = new HashMap<>(properties.size());
        for (PropertyInfo pmd : properties) {
            propertyMap.put(pmd.getName(), pmd);
        }
    }

    public List<PropertyInfo> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public Map<String, PropertyInfo> getPropertyMap() {
        return Collections.unmodifiableMap(propertyMap);
    }

    public PropertyInfo getProperty(String name) {
        return propertyMap.get(name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BeanInfo{");
        sb.append("properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }
}
