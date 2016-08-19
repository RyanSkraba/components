package org.talend.components.fileinput.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;

public class FileInputIndexedRecord implements IndexedRecord {

	private Schema schema;

	private String values;

	public FileInputIndexedRecord(String values, Schema schema) {
		this.values = values;
		this.schema = schema;
	}

	@Override
	public void put(int index, Object json) {
		throw new UnsupportedOperationException();
	}

	private String names[];

	AvroConverter[] fieldConverter = null;

	@Override
	public Object get(int index) {
		// Lazy initialization of the cached converter objects.
		if (names == null) {
			names = new String[getSchema().getFields().size()];
			fieldConverter = new AvroConverter[names.length];
			for (int j = 0; j < names.length; j++) {
				Field f = getSchema().getFields().get(j);
				names[j] = f.name();
				fieldConverter[j] = new FileInputAvroRegistry().getConverterFromString(f);
			}
		}
		return fieldConverter[index].convertToAvro(values);
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

}

class InputResult {

	Map<String, Object> values;

	public InputResult() {
		values = new HashMap<String, Object>();
	}

	public void setValue(String field, Object vlaue) {
		values.put(field, vlaue);
	}

	public Object getValue(String fieldName) {
		return values.get(fieldName);
	}

	public void copyValues(InputResult result) {
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
}
