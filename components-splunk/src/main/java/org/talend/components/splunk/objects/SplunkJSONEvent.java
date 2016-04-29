// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.splunk.objects;

import java.util.Map;

import org.json.simple.JSONObject;

public class SplunkJSONEvent extends JSONObject {

    private static final long serialVersionUID = 3205801702373951474L;

    public SplunkJSONEvent() {
    }

    public void setEventObjects(Map<String, Object> eventObjects) {
        JSONObject jsonEventObject = new JSONObject(eventObjects);
        put(SplunkJSONEventField.EVENT, jsonEventObject);
    }

    public void addEventObject(String key, Object object) {
        if (key == null || object == null) {
            return;
        }
        JSONObject jsonEventObject = (JSONObject) get(SplunkJSONEventField.EVENT.getName());
        if (jsonEventObject == null) {
            jsonEventObject = new JSONObject();
            put(SplunkJSONEventField.EVENT, jsonEventObject);
        }
        jsonEventObject.put(key, object);
    }

    public void put(String name, Object value) {
        if (name == null || value == null) {
            return;
        }
        super.put(name, value);
    }

    public void put(SplunkJSONEventField key, Object value) {
        if (key == null || value == null) {
            return;
        }
        super.put(key.getName(), value);
    }

}
