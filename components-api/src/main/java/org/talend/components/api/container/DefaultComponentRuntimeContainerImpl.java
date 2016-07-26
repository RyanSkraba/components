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
package org.talend.components.api.container;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of a runtime container for testing purposes.
 */
public class DefaultComponentRuntimeContainerImpl implements RuntimeContainer {

    private Map<String, Object> map = new HashMap();

    @Override
    public Object getComponentData(String componentId, String key) {
        return map.get(componentId + key);
    }

    @Override
    public void setComponentData(String componentId, String key, Object data) {
        map.put(componentId + key, data);
    }

    @Override
    public String getCurrentComponentId() {
        return null;
    }

    @Override
    public Object getGlobalData(String key) {
        return map.get(key);
    }
}
