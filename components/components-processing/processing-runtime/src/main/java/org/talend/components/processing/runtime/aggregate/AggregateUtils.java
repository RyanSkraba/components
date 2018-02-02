package org.talend.components.processing.runtime.aggregate;

import static org.talend.components.adapter.beam.kv.SchemaGeneratorUtils.TREE_ROOT_DEFAULT_VALUE;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.adapter.beam.kv.SchemaGeneratorUtils;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.processing.definition.aggregate.AggregateFieldOperationType;
import org.talend.components.processing.definition.aggregate.AggregateOperationProperties;
import org.talend.components.processing.definition.aggregate.AggregateProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class AggregateUtils {

    /**
     * Based on incoming record's schema and the operation setting in {@code AggregateProperties},
     * generate the output record's schema
     * 
     * @param inputRecordSchema
     * @param props
     * @return
     */
    public static Schema genOutputRecordSchema(Schema inputRecordSchema, AggregateProperties props) {
        Map<String, Set<Object>> container = new HashMap<>();

        for (AggregateOperationProperties operationProps : props.filteredOperations()) {
            fillNewFieldTreeByAggFunc(container, operationProps, inputRecordSchema);
        }

        return SchemaGeneratorUtils.convertTreeToAvroSchema(container, TREE_ROOT_DEFAULT_VALUE, inputRecordSchema);
    }

    /**
     * Based on incoming record's schema and the operation setting in {@code AggregateProperties},
     * generate the schema of one field in output record
     *
     * @param inputSchema
     * @param operationProps
     * @return
     */
    public static Schema genOutputFieldSchema(Schema inputSchema, AggregateOperationProperties operationProps) {
        Map<String, Set<Object>> container = new HashMap<>();

        fillNewFieldTreeByAggFunc(container, operationProps, inputSchema);

        return SchemaGeneratorUtils.convertTreeToAvroSchema(container, TREE_ROOT_DEFAULT_VALUE, inputSchema);
    }

    /**
     * Generate output field path,
     * if the user did not set an output field path, use the input field name and the operation name
     * if the user set an output field path, use the name of the last element in the path.
     *
     * @param operationProps
     * @return
     */
    public static String genOutputFieldPath(AggregateOperationProperties operationProps) {
        return StringUtils.isEmpty(operationProps.outputFieldPath.getValue())
                ? genOutputFieldNameByOpt(operationProps.fieldPath.getValue(), operationProps.operation.getValue())
                : operationProps.outputFieldPath.getValue();
    }

    private static String genOutputFieldNameByOpt(String originalName, AggregateFieldOperationType operationType) {
        return originalName + "_" + operationType.toString();
    }

    private static void fillNewFieldTreeByAggFunc(Map<String, Set<Object>> container,
            AggregateOperationProperties operationProps, Schema inputSchema) {
        String path = genOutputFieldPath(operationProps);
        Schema.Field newField = genField(
                SchemaGeneratorUtils.retrieveFieldFromJsonPath(inputSchema, operationProps.fieldPath.getValue()),
                operationProps);

        fillNewFieldTree(container, path, newField);
    }

    private static void fillNewFieldTree(Map<String, Set<Object>> container, String path, Schema.Field field) {
        String currentParent = TREE_ROOT_DEFAULT_VALUE;
        String[] splittedPath = path.split("\\.");
        for (int i = 0; i < splittedPath.length - 1; i++) {
            if (!container.containsKey(currentParent)) {
                container.put(currentParent, new LinkedHashSet<>());
            }
            container.get(currentParent).add(currentParent + "." + splittedPath[i]);
            currentParent = currentParent + "." + splittedPath[i];
        }
        // Add the field into the tree
        if (!container.containsKey(currentParent)) {
            container.put(currentParent, new LinkedHashSet<>());
        }

        container.get(currentParent).add(field);
    }

    /**
     * Generate new field,
     * if the user did not set an output field path, use the input field name and the operation name
     * if the user set an output field path, use the name of the last element in the path.
     *
     * @param originalField
     * @param operationProps
     * @return
     */
    public static Schema.Field genField(Schema.Field originalField, AggregateOperationProperties operationProps) {
        Schema newFieldSchema =
                AvroUtils.wrapAsNullable(genFieldType(originalField.schema(), operationProps.operation.getValue()));
        String outputFieldPath = operationProps.outputFieldPath.getValue();
        String newFieldName;
        if (StringUtils.isEmpty(outputFieldPath)) {
            newFieldName = genOutputFieldNameByOpt(originalField.name(), operationProps.operation.getValue());
        } else {
            newFieldName = outputFieldPath.contains(".") ? StringUtils.substringAfterLast(outputFieldPath, ".")
                    : outputFieldPath;
        }
        return new Schema.Field(newFieldName, newFieldSchema, originalField.doc(), originalField.defaultVal());
    }

    /**
     * Generate new field type,
     * assume output field type according to input field type and operation type
     *
     * @param fieldType
     * @param operationType
     * @return
     */
    public static Schema genFieldType(Schema fieldType, AggregateFieldOperationType operationType) {
        switch (operationType) {
        case LIST: {
            return Schema.createArray(fieldType);
        }
        case COUNT: {
            return AvroUtils._long();
        }
        default:
            fieldType = AvroUtils.unwrapIfNullable(fieldType);
            if (!AvroUtils.isNumerical(fieldType.getType())) {
                TalendRuntimeException.build(ComponentsErrorCode.SCHEMA_TYPE_MISMATCH).setAndThrow("aggregate",
                        "int/long/float/double", fieldType.getType().getName());
            }
            switch (operationType) {
            case SUM:
                if (AvroUtils.isSameType(fieldType, AvroUtils._int())) {
                    return AvroUtils._long();
                } else if (AvroUtils.isSameType(fieldType, AvroUtils._float())) {
                    return AvroUtils._double();
                }
                // else double and long
                return fieldType;
            case AVG:
                return AvroUtils._double();
            case MIN:
            case MAX:
                return fieldType;
            }
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).throwIt();
            break;
        }
        return fieldType;
    }

    /**
     * Set the value to record based on the fieldPath, if the path is wrong nothing will be added on record
     *
     * @param fieldPath
     * @param value
     * @param record
     */
    public static void setField(String fieldPath, Object value, IndexedRecord record) {
        String[] path = fieldPath.split("\\.");
        if (path.length <= 0) {
            return;
        }
        Schema schema = record.getSchema();
        IndexedRecord parentRecord = record;
        for (int i = 0; i < path.length - 1; i++) {
            if (schema.getField(path[i]) == null) {
                return;
            }

            Object parentRecordObj = parentRecord.get(schema.getField(path[i]).pos());

            if (parentRecordObj instanceof GenericData.Record) {
                parentRecord = (IndexedRecord) parentRecordObj;

                // The sub-schema at this level is a union of "empty" and a
                // record, so we need to get the true sub-schema
                if (schema.getField(path[i]).schema().getType().equals(Schema.Type.RECORD)) {
                    schema = schema.getField(path[i]).schema();
                } else if (schema.getField(path[i]).schema().getType().equals(Schema.Type.UNION)) {
                    for (Schema childSchema : schema.getField(path[i]).schema().getTypes()) {
                        if (childSchema.getType().equals(Schema.Type.RECORD)) {
                            schema = childSchema;
                            break;
                        }
                    }
                }
            } else {
                // can't find parent record, can't fill value
                return;
            }
        }

        parentRecord.put(schema.getField(path[path.length - 1]).pos(), value);
    }
}
