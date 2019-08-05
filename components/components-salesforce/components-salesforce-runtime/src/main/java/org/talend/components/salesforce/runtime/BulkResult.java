package org.talend.components.salesforce.runtime;

import java.util.Map;
import java.util.TreeMap;

public class BulkResult {

    Map<String, Object> values;

    public BulkResult() {
        values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public void setValue(String field, Object vlaue) {
        values.put(field, vlaue);
    }

    public Object getValue(String fieldName) {
        return values.get(fieldName);
    }

    public void copyValues(BulkResult result) {
        if (result == null) {
            return;
        } else {
            for (String key : result.values.keySet()) {
                Object value = result.values.get(key);
                if ("#N/A".equals(value)) {
                    value = null;
                }
                values.put(key, value);
            }
        }
    }

    public boolean containField(String fieldName) {
        if (values != null && values.containsKey(fieldName)) {
            return true;
        }
        return false;
    }
}