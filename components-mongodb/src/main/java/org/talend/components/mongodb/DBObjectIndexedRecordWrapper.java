package org.talend.components.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.IndexedRecord;

import com.mongodb.DBObject;

/**
 * Wraps a MongoDB implementation-specific {@link DBObject} in an {@link IndexedRecord} so that subsequent components
 * can access it in a standard way.
 */
public class DBObjectIndexedRecordWrapper implements IndexedRecord {

    /** The wrapped, original DBObject. */
    private DBObject mWrapped;

    transient private Schema mSchema;

    transient private Map<String, Integer> mNameToPosition;

    private DBObjectIndexedRecordWrapper() {
        // For Kryo (for now). To be removed when the AvroCoder is added.
    }

    public DBObjectIndexedRecordWrapper(DBObject next) {
        setWrapped(next, null);
    }

    @Override
    public Schema getSchema() {
        if (mSchema == null) {
            mNameToPosition = new HashMap<>();
            mSchema = Schema.createRecord("DbObjectRecord", null, null, false);
            List<Field> fields = new ArrayList<Field>();
            List<String> fieldNames = new ArrayList<>(mWrapped.keySet());
            int i = 0;
            for (String key : fieldNames) {
                // Just use a fake type for the moment.
                fields.add(new Field(key, Schema.create(Type.BYTES), null, null));
                mNameToPosition.put(key, i++);
            }
            mSchema.setFields(fields);
        }
        return mSchema;
    }

    @Override
    public void put(int i, Object v) {
        mWrapped.put(getSchema().getFields().get(i).name(), v);
    }

    @Override
    public Object get(int i) {
        return mWrapped.get(getSchema().getFields().get(i).name());
    }

    public void setWrapped(DBObject wrapped, Schema schema) {
        mWrapped = wrapped;
        mSchema = schema;
    }
}
