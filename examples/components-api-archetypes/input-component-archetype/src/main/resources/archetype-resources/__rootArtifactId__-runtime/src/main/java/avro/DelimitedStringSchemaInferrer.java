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
package ${package}.avro;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.talend.daikon.avro.AvroUtils;

/**
 * Creates (infers) {@link Schema} from data, which is read from data storage
 * This is used in case user specifies dynamic field in Design schema
 */
public class DelimitedStringSchemaInferrer {

    /**
     * Default schema for dynamic fields are of type String
     */
    private static final Schema STRING_SCHEMA = AvroUtils._string();

    /**
     * Field delimiter which is used in string line
     */
    private final String delimiter;

    /**
     * Constructors sets delimiter
     */
    public DelimitedStringSchemaInferrer(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Creates Runtime schema from incoming data. <br>
     * Schema is created in following way: <br>
     * 1. Delimited string is splitted using <code>delimiter</code> to count
     * number of fields in delimited string <br>
     * 2. The same number of fields are created for Runtime schema <br>
     * 3. Field names are {@code "column<Index>"} <br>
     * 4. Field types are String
     * 
     * @param delimitedString a line, which was read from file source
     * @return Runtime avro schema
     */
    public Schema inferSchema(String delimitedString) {
        String[] fields = delimitedString.split(delimiter);
        int size = fields.length;
        List<Field> schemaFields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Field designField = new Field("column" + i, STRING_SCHEMA, null, (Object) null);
            schemaFields.add(i, designField);
        }
        Schema schema = Schema.createRecord("Runtime", null, null, false, schemaFields);
        return schema;
    }

}
