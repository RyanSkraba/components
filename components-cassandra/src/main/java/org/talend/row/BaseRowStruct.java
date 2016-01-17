package org.talend.row;

import org.talend.components.api.schema.SchemaElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bchen on 16-1-10.
 */
public class BaseRowStruct {
    Map<String, Object> content = new HashMap<>();

    Map<String, SchemaElement.Type> metadata = new HashMap<>();

    public BaseRowStruct(Map<String, SchemaElement.Type> metadata) {
        this.metadata = metadata;
    }

    public SchemaElement.Type getType(String key) {
        return metadata.get(key);
    }

    public Object get(String key) {
        return content.get(key);
    }

    public void put(String key, Object value) {
//        if (metadata.get(key).equals(value)) {
        content.put(key, value);
//        } else {
//            // FIXME use talend exception
//            throw new RuntimeException("unsupport set " + value.getClass() + " type to " + metadata.get(key));
//        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (String k : content.keySet()) {
            s.append(k);
            s.append(":");
            s.append(content.get(k));
            s.append("\n");
        }
        return s.toString();
    }
}
