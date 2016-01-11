package org.talend.row;

import org.talend.schema.type.TBaseType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bchen on 16-1-10.
 */
public class BaseRowStruct {
    Map<String, TBaseType> content = new HashMap<>();

    Map<String, Class<? extends TBaseType>> metadata = new HashMap<>();

    public BaseRowStruct(Map<String, Class<? extends TBaseType>> metadata) {
        this.metadata = metadata;
        for (String key : this.metadata.keySet()) {
            try {
                this.content.put(key, this.metadata.get(key).newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Class<? extends TBaseType> getType(String key) {
        return metadata.get(key);
    }

    public TBaseType get(String key) {
        return content.get(key);
    }

    public void put(String key, TBaseType value) {
        if (metadata.get(key).equals(value.getClass())) {
            content.get(key).setValue(value.getValue());
        } else {
            // FIXME use talend exception
            throw new RuntimeException("unsupport set " + value.getClass() + " type to " + metadata.get(key));
        }
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
