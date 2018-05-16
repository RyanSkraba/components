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
package org.talend.components.adapter.beam.kv;

import static org.talend.components.adapter.beam.kv.KeyValueRecordConstant.RECORD_KEYVALUE_PREFIX;
import static org.talend.components.adapter.beam.kv.KeyValueRecordConstant.RECORD_KEY_PREFIX;
import static org.talend.components.adapter.beam.kv.KeyValueRecordConstant.RECORD_VALUE_PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.StringUtils;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class SchemaGeneratorUtils {

    public static final String TREE_ROOT_DEFAULT_VALUE = "$";

    /**
     * Create a new schema by extracting elements from the inputSchema that are present in the keyPaths
     * 
     * @param inputSchema a schema
     * @param keyPaths a list of path to element that will be considered as keys
     * @return a new schema
     */
    public static Schema extractKeys(Schema inputSchema, List<String> keyPaths) {
        if (inputSchema == null) {
            return AvroUtils.createEmptySchema();
        }
        // Generate the subSchema as a tree
        Map<String, Set<Object>> tree = generateTree(inputSchema, keyPaths);
        // use the generated tree to create an avro Schema
        Schema schema = convertTreeToAvroSchema(tree, TREE_ROOT_DEFAULT_VALUE, inputSchema);
        if (schema.getName() == null) {
            schema = Schema.createRecord(RECORD_KEY_PREFIX, schema.getDoc(), schema.getNamespace(), schema.isError(),
                    schema.getFields());
        }
        return schema;
    }

    /**
     * Use a JSONPath to retrieve a field from an Avro Schema
     * 
     * @param inputSchema The avro Schema
     * @param path the Json Path
     * @return the Field if it exist otherwise return null
     */
    public static Field retrieveFieldFromJsonPath(Schema inputSchema, String path) {
        if (path.startsWith("$.")) {
            path = path.substring(2);
        }
        Schema currentSchema = inputSchema;
        Field output = null;
        for (String pathElement : path.split("\\.")) {
            output = AvroUtils.unwrapIfNullable(currentSchema).getField(pathElement);
            if (output == null) {
                break;
            }
            currentSchema = output.schema();
        }
        return output;
    }

    /**
     * Use a JSONPath to retrieve a field from an key/value Avro Schema
     * 
     * @param keySchema An avro Schema
     * @param valueSchema An avro Schema
     * @param path the Json Path
     * @return the Field if it exist in the key or the value schema otherwise return null
     */
    public static Field retrieveFieldFromJsonPath(Schema keySchema, Schema valueSchema, String path) {
        Field output = retrieveFieldFromJsonPath(valueSchema, path);
        if (output == null) {
            return retrieveFieldFromJsonPath(keySchema, path);
        } else {
            return output;
        }
    }

    /**
     * Use a list of JSON path to extract a subschema from an input schema. Will return the new subschema as a tree
     * inside a Hashmap.
     * 
     * This is a method is part of a processing algorithm in order to generate a new avro schema. We need this
     * computation in order to aggregate the different node of the avro schema.
     * 
     * @param inputSchema A reference schema used to retrieve the types of the different fields.
     * @param fieldPaths The list of JSONpath to extract. Any new element will create a new Field set by default to a
     * null string.
     * @return an hashmap representing the avro tree
     */
    private static Map<String, Set<Object>> generateTree(Schema inputSchema, List<String> fieldPaths) {
        Map<String, Set<Object>> tree = new HashMap<>();
        for (String fieldPath : fieldPaths) {
            String currentParrentValue = TREE_ROOT_DEFAULT_VALUE;
            String[] splittedPath = fieldPath.split("\\.");
            String currentElement = splittedPath[splittedPath.length - 1];

            // Add the path to the field into the tree
            for (int i = 0; i < splittedPath.length - 1; i++) {
                if (!tree.containsKey(currentParrentValue)) {
                    tree.put(currentParrentValue, new LinkedHashSet<>());
                }
                tree.get(currentParrentValue).add(currentParrentValue + "." + splittedPath[i]);
                currentParrentValue = currentParrentValue + "." + splittedPath[i];
            }

            // Add the field into the tree
            if (!tree.containsKey(currentParrentValue)) {
                tree.put(currentParrentValue, new LinkedHashSet<>());
            }
            if (retrieveFieldFromJsonPath(inputSchema, fieldPath) != null) {
                // If the field exist in the inputSchema, copy its schema
                Field oldField = retrieveFieldFromJsonPath(inputSchema, fieldPath);
                tree.get(currentParrentValue).add(
                        new Field(currentElement, oldField.schema(), oldField.doc(), oldField.defaultVal()));
            } else {
                // If the field does not exist in the inputSchema, create a new element as a String Field
                Schema outputHierarchicalSchema =
                        SchemaBuilder.builder().unionOf().nullType().and().stringType().endUnion();
                tree.get(currentParrentValue).add(new Field(currentElement, outputHierarchicalSchema, "", ""));
            }
        }
        return tree;
    }

    /**
     * Generate the Avro schema from a tree representation of the schema.
     * 
     * @param tree Hashmap representing a tree generated by the method generateTree()
     * @param elementToGenerate the current part of the tree that will be generated.
     * @return
     */
    public static Schema convertTreeToAvroSchema(Map<String, Set<Object>> tree, String elementToGenerate,
            Schema inputSchema) {
        List<Schema.Field> fieldList = new ArrayList<>();
        if (tree.containsKey(elementToGenerate)) {
            for (Object treeElement : tree.get(elementToGenerate)) {
                if (treeElement instanceof String) {
                    // path element, generate the schema of the subtree then add it as a field.
                    Schema subElementSchema = convertTreeToAvroSchema(tree, (String) treeElement, inputSchema);
                    String elementName = (String) treeElement;
                    if (elementName.contains(".")) {
                        elementName = StringUtils.substringAfterLast(elementName, ".");
                    }
                    fieldList.add(new Field(elementName, subElementSchema, "", ""));
                } else if (treeElement instanceof Field) {
                    // field element, adding it to the field list.
                    fieldList.add((Field) treeElement);
                } else {
                    TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).setAndThrow(
                            "Should be only String or Field", treeElement.getClass().toString());
                }
            }
        } else {
            if (!TREE_ROOT_DEFAULT_VALUE.equals(elementToGenerate)) {
                TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).setAndThrow(tree.keySet().toString(),
                        elementToGenerate);
            }
        }

        try {
            if (inputSchema == null) {
                return Schema.createRecord(fieldList);
            } else if ("$".equals(elementToGenerate)) {
                return Schema.createRecord(inputSchema.getName(), inputSchema.getDoc(), inputSchema.getNamespace(),
                        inputSchema.isError(), fieldList);
            } else if (retrieveFieldFromJsonPath(inputSchema, elementToGenerate) != null) {
                // If the field exist in the inputSchema, copy its schema
                Schema currentSchema = retrieveFieldFromJsonPath(inputSchema, elementToGenerate).schema();
                return Schema.createRecord(currentSchema.getName(), currentSchema.getDoc(),
                        currentSchema.getNamespace(), currentSchema.isError(), fieldList);
            } else {
                return Schema.createRecord(fieldList);
            }
        } catch (AvroRuntimeException e) {
            // this will be throw if we are trying to get the name of an anonymous type
            return Schema.createRecord(fieldList);
        }
    }

    /**
     * Create a new schema by extracting elements from the inputSchema that are *not* present in the keyPaths
     * 
     * @param inputSchema a schema
     * @param keyPaths a list of path to element that will be considered as keys
     * @return a new schema
     */
    public static Schema extractValues(Schema inputSchema, List<String> keyPaths) {
        if (inputSchema == null) {
            return AvroUtils.createEmptySchema();
        }
        Schema schema = extractValues(inputSchema, keyPaths, "");
        if (schema == null) {
            return AvroUtils.createEmptySchema();
        }
        if (schema.getName() == null) {
            schema = Schema.createRecord(RECORD_VALUE_PREFIX, schema.getDoc(), schema.getNamespace(), schema.isError(),
                    schema.getFields());
        }
        return schema;
    }

    /**
     * Create a new schema by extracting elements from the inputSchema that are *not* present in the keyPaths
     * 
     * @param inputSchema a schema
     * @param keyPaths a list of path to element that will be considered as keys
     * @param currentPath the current subelement to extract
     * @return a new schema
     */
    private static Schema extractValues(Schema inputSchema, List<String> keyPaths, String currentPath) {
        List<Schema.Field> fieldList = new ArrayList<>();
        for (Field field : inputSchema.getFields()) {
            String newPath = currentPath + "." + field.name();
            if (StringUtils.isEmpty(currentPath)) {
                newPath = currentPath + field.name();
            }
            if (keyPaths.contains(newPath)) {
                // Do nothing
            } else {
                Schema unwrappedSchema = getUnwrappedSchema(field);
                if (unwrappedSchema.getType().equals(Type.RECORD)) {
                    Schema subElementSchema = extractValues(unwrappedSchema, keyPaths, newPath);
                    if (subElementSchema != null) {
                        fieldList.add(new Field(field.name(), subElementSchema, "", ""));
                    }
                } else {
                    // element add it directly
                    fieldList.add(new Field(field.name(), field.schema(), field.doc(), field.defaultVal()));
                }
            }
        }
        if (fieldList.size() > 0) {
            try {
                return Schema.createRecord("value_" + inputSchema.getName(), inputSchema.getDoc(),
                        inputSchema.getNamespace(), inputSchema.isError(), fieldList);
            } catch (AvroRuntimeException e) {
                // this will be throw if we are trying to get the name of an anonymous type
                return Schema.createRecord(fieldList);
            }
        } else {
            return null;
        }
    }

    /**
     * Create a KV-SChema from a schema and a list of keys
     * 
     * @param inputSchema a Schema
     * @param keyPaths the list of key
     * @return a hierarchical KV-Schema
     */
    public static Schema extractKeyValues(Schema inputSchema, List<String> keyPaths) {
        List<Schema.Field> fieldList = new ArrayList<>();

        Schema keySchema = extractKeys(inputSchema, keyPaths);
        fieldList.add(new Field(RECORD_KEY_PREFIX, keySchema, "", ""));
        Schema valueSchema = extractValues(inputSchema, keyPaths);
        fieldList.add(new Field(RECORD_VALUE_PREFIX, valueSchema, "", ""));
        return Schema.createRecord(RECORD_KEYVALUE_PREFIX, null, null, false, fieldList);
    }

    /**
     * Merge a KV-Schema into a single schema.
     * 
     * For each level, the schema will contains the elements present in the keySchema first, then the ones present in
     * the valueSchema.
     * 
     * @param keySchema an avro Schema
     * @param valueSchema an avro Schema
     * @return an avro Schema merging the two previous schema
     */
    public static Schema mergeKeyValues(Schema keySchema, Schema valueSchema) {
        List<Schema.Field> fieldList = new ArrayList<>();
        for (Field field : keySchema.getFields()) {
            if (valueSchema.getField(field.name()) != null) {
                // element in both key and value => create sub element
                fieldList.add(new Field(field.name(),
                        mergeKeyValues(field.schema(), valueSchema.getField(field.name()).schema()), "", ""));
            } else {
                // Element only present in the key
                fieldList.add(new Field(field.name(), field.schema(), field.doc(), field.defaultVal()));
            }
        }

        for (Field field : valueSchema.getFields()) {
            if (keySchema.getField(field.name()) == null) {
                // Element only present in the value
                fieldList.add(new Field(field.name(), field.schema(), field.doc(), field.defaultVal()));
            }
        }
        if (fieldList.size() > 0) {
            try {
                return Schema.createRecord(keySchema.getName(), keySchema.getDoc(), keySchema.getNamespace(),
                        keySchema.isError(), fieldList);
            } catch (AvroRuntimeException e) {
                // this will be throw if we are trying to get the name of an anonymous type
                return Schema.createRecord(fieldList);
            }
        } else {
            return AvroUtils.createEmptySchema();
        }
    }

    /**
     * Merge a KV-SChema into a single schema
     * 
     * @param inputSchema a Schema
     * @return an avro Schema resulting in a merge of the key and the value part of the KV schema
     */
    public static Schema mergeKeyValues(Schema inputSchema) {
        return mergeKeyValues(inputSchema.getField(RECORD_KEY_PREFIX).schema(),
                inputSchema.getField(RECORD_VALUE_PREFIX).schema());
    }

    private static Schema getUnwrappedSchema(Field field) {
        return AvroUtils.unwrapIfNullable(field.schema());
    }

}
