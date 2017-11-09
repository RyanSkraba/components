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
package org.talend.components.marketo.runtime.client.rest.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketoAttributes {

    private List<Map<String, Object>> attributes;

    public List<Map<String, Object>> getAttributes() {
        // ensure that attributes is never null
        if (attributes == null) {
            return new ArrayList<>();
        }

        return attributes;
    }

    public void setAttributes(List<Map<String, Object>> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getMktoAttributes() {
        Map<String, String> result = new HashMap<String, String>();
        for (Map<String, Object> t : getAttributes()) {
            result.put(String.valueOf(t.get("name")).replace(" ", "_"), String.valueOf(t.get("value")));
        }

        return result;
    }

}
