package org.talend.components.s3.runtime;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class PropertiesPreparer {

    public static String accessKey = System.getProperty("s3.accesskey");

    public static String secretkey = System.getProperty("s3.secretkey");

    public static String bucket = System.getProperty("s3.bucket");

    public static String ssekmskey = System.getProperty("s3.ssekmskey");

    public static String csekmskey = System.getProperty("s3.csekmskey");

    public static String objectkey = "data";

    public static S3OutputProperties createS3OtuputProperties() {
        S3OutputProperties properties = new S3OutputProperties("s3output");
        S3DatasetProperties dataset = new S3DatasetProperties("dataset");
        S3DatastoreProperties datastore = new S3DatastoreProperties("datastore");

        properties.setDatasetProperties(dataset);
        dataset.setDatastoreProperties(datastore);

        datastore.accessKey.setValue(accessKey);
        datastore.secretKey.setValue(secretkey);
        dataset.bucket.setValue(bucket);
        dataset.kmsForDataInMotion.setValue(ssekmskey);
        dataset.kmsForDataAtRest.setValue(csekmskey);

        dataset.object.setValue(objectkey);

        return properties;
    }

    public static Schema createTestSchema() {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record("TEST").fields();

        Schema schema = AvroUtils._int();
        schema = wrap(schema);
        builder = builder.name("ID").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID").type(schema).noDefault();

        schema = AvroUtils._string();
        schema = wrap(schema);
        builder = builder.name("NAME").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME").type(schema).noDefault();

        return builder.endRecord();
    }

    private static Schema wrap(Schema schema) {
        return SchemaBuilder.builder().nullable().type(schema);
    }

    private static Schema wrap(Schema schema, boolean nullable) {
        if (nullable) {
            return wrap(schema);
        }

        return schema;
    }
}
