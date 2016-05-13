package org.talend.components.salesforce.runtime;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroConverter;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Creates an {@link IndexedRecordAdapterFactory} that knows how to interpret Salesforce {@link SObject} objects.
 */
public class SObjectAdapterFactory implements IndexedRecordAdapterFactory<SObject, IndexedRecord> {

    private Schema schema;

    private String names[];

    /**
     * The cached AvroConverter objects for the fields of this record.
     */
    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Class<SObject> getDatumClass() {
        return SObject.class;
    }

    @Override
    public SObject convertToDatum(IndexedRecord value) {
        throw new UnmodifiableAdapterException();
    }

    @Override
    public IndexedRecord convertToAvro(SObject value) {
        return new SObjectIndexedRecord(value);
    }

    private class SObjectIndexedRecord implements IndexedRecord {

        private Map<String, Object> valueMap;

        public SObjectIndexedRecord(SObject value) {
            Iterator<XmlObject> fields = value.getChildren();
            while (fields.hasNext()) {
                processXmlObject(fields.next(), null);
            }
        }

        @Override
        public Schema getSchema() {
            return SObjectAdapterFactory.this.getSchema();
        }

        @Override
        public void put(int i, Object v) {
            throw new UnmodifiableAdapterException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object get(int i) {
            // Lazy initialization of the cached converter objects.
            if (names == null) {
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = SalesforceAvroRegistry.get().getConverterFromString(f);
                }
            }
            return fieldConverter[i].convertToAvro(valueMap.get(names[i]));
        }

        protected void processXmlObject(XmlObject xo, String prefixName) {
            if (valueMap == null) {
                valueMap = new HashMap<>();
            }
            Iterator<XmlObject> xos = xo.getChildren();
            if (xos.hasNext()) {
                // delete the fixed id and type elements when find firstly
                int typeCount = 0;
                int idCount = 0;
                while (xos.hasNext()) {
                    XmlObject objectValue = xos.next();
                    if (objectValue != null) {
                        XmlObject xmlObject = (XmlObject) objectValue;

                        if ("type".equals(xmlObject.getName().getLocalPart()) && typeCount == 0) {
                            typeCount++;
                            continue;
                        }
                        if ("Id".equals(xmlObject.getName().getLocalPart()) && idCount == 0) {
                            idCount++;
                            continue;
                        }
                        if (prefixName != null) {
                            processXmlObject(xmlObject, prefixName + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER) + xo.getName().getLocalPart());
                        } else {
                            processXmlObject(xmlObject, xo.getName().getLocalPart());
                        }
                    }
                }
            } else {
                Object value = xo.getValue();
                if (value == null || "".equals(value)) {
                    return;
                }
                String columnName = null;
                if (prefixName != null && prefixName.length() > 0) {
                    columnName = prefixName + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER) + xo.getName().getLocalPart();
                } else {
                    columnName = xo.getName().getLocalPart();
                }
                if (valueMap.get(columnName) == null) {
                    valueMap.put(columnName, value);
                } else {
                    if (!columnName.equals(xo.getName().getLocalPart())) {
                        valueMap.put(columnName, valueMap.get(columnName) + schema.getProp(SalesforceSchemaConstants.VALUE_DELIMITER) + value);
                    }
                }
            }
        }
    }
}
