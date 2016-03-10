package org.talend.components.salesforce.runtime;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroConverter;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime.BulkResult;

/**
 *
 */
public class BulkResultAdapterFactory implements IndexedRecordAdapterFactory<BulkResult, IndexedRecord> {

    private Schema schema;

    private String names[];

    /** The cached AvroConverter objects for the fields of this record. */
    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public Class<BulkResult> getDatumClass() {
        return BulkResult.class;
    }

    @Override
    public BulkResult convertToDatum(IndexedRecord indexedRecord) {
        throw new UnmodifiableAdapterException();
    }

    @Override
    public IndexedRecord convertToAvro(BulkResult result) {
        return new ResultIndexedRecord(result);
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    private class ResultIndexedRecord implements IndexedRecord {

        private final BulkResult value;

        public ResultIndexedRecord(BulkResult value) {
            this.value = value;
        }

        @Override
        public void put(int i, Object o) {
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
            return fieldConverter[i].convertToAvro(value.getValue(names[i]));
        }

        @Override
        public Schema getSchema() {
            return BulkResultAdapterFactory.this.getSchema();
        }
    }
}
