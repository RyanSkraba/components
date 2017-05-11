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

package org.talend.components.netsuite;

import static org.talend.components.netsuite.client.model.beans.Beans.toInitialLower;
import static org.talend.components.netsuite.client.model.beans.Beans.toInitialUpper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorName;
import org.talend.components.netsuite.schema.SearchFieldInfo;
import org.talend.components.netsuite.schema.SearchInfo;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.di.DiSchemaConstants;

/**
 *
 */
public class NetSuiteDatasetRuntimeImpl implements NetSuiteDatasetRuntime {
    private MetaDataSource metaDataSource;

    public NetSuiteDatasetRuntimeImpl(MetaDataSource metaDataSource) throws NetSuiteException {
        this.metaDataSource = metaDataSource;
    }

    @Override
    public List<NamedThing> getRecordTypes() {
        try {
            Collection<RecordTypeInfo> recordTypeList = metaDataSource.getRecordTypes();

            List<NamedThing> recordTypes = new ArrayList<>(recordTypeList.size());
            for (RecordTypeInfo recordTypeInfo : recordTypeList) {
                recordTypes.add(new SimpleNamedThing(recordTypeInfo.getName(), recordTypeInfo.getDisplayName()));
            }

            // Sort by display name alphabetically
            Collections.sort(recordTypes, new Comparator<NamedThing>() {
                @Override public int compare(NamedThing o1, NamedThing o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            });
            return recordTypes;
        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Schema getSchema(String typeName) {
        try {
            final RecordTypeInfo recordTypeInfo = metaDataSource.getRecordType(typeName);
            final TypeDesc typeDesc = metaDataSource.getTypeInfo(typeName);

            List<FieldDesc> fieldDescList = new ArrayList<>(typeDesc.getFields());
            Collections.sort(fieldDescList, FieldDescComparator.INSTANCE);

            Schema schema = inferSchemaForType(typeDesc.getTypeName(), fieldDescList);
            augmentSchemaWithCustomMetaData(schema, recordTypeInfo, fieldDescList);

            return schema;
        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public List<NamedThing> getSearchableTypes() {
        try {
            List<NamedThing> searchableTypes = new ArrayList<>(metaDataSource.getSearchableTypes());
            // Sort by display name alphabetically
            Collections.sort(searchableTypes, new Comparator<NamedThing>() {
                @Override public int compare(NamedThing o1, NamedThing o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            });
            return searchableTypes;
        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public SearchInfo getSearchInfo(String typeName) {
        try {
            final SearchRecordTypeDesc searchInfo = metaDataSource.getSearchRecordType(typeName);
            final TypeDesc searchRecordInfo = metaDataSource.getBasicMetaData()
                    .getTypeInfo(searchInfo.getSearchBasicClass());

            List<FieldDesc> fieldDescList = searchRecordInfo.getFields();

            List<SearchFieldInfo> fields = new ArrayList<>(fieldDescList.size());
            for (FieldDesc fieldDesc : fieldDescList) {
                SearchFieldInfo field = new SearchFieldInfo(fieldDesc.getName(), fieldDesc.getValueType());
                fields.add(field);
            }
            // Sort by name alphabetically
            Collections.sort(fields, new Comparator<SearchFieldInfo>() {
                @Override public int compare(SearchFieldInfo o1, SearchFieldInfo o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            return new SearchInfo(searchRecordInfo.getTypeName(), fields);

        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Schema getSchemaForUpdate(String typeName) {
        try {
            final RecordTypeInfo recordTypeInfo = metaDataSource.getRecordType(typeName);
            final TypeDesc typeDesc = metaDataSource.getTypeInfo(typeName);

            List<FieldDesc> fieldDescList = new ArrayList<>(typeDesc.getFields());
            Collections.sort(fieldDescList, FieldDescComparator.INSTANCE);

            Schema schema = inferSchemaForType(typeDesc.getTypeName(), fieldDescList);
            augmentSchemaWithCustomMetaData(schema, recordTypeInfo, typeDesc.getFields());

            return schema;
        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Schema getSchemaForDelete(String typeName) {
        return getSchemaForRecordRef(typeName);
    }

    public Schema getSchemaForRecordRef(String typeName) {
        try {
            final RecordTypeInfo referencedRecordTypeInfo = metaDataSource.getRecordType(typeName);
            final RefType refType = referencedRecordTypeInfo.getRefType();
            final TypeDesc typeDesc = metaDataSource.getTypeInfo(refType.getTypeName());

            List<FieldDesc> fieldDescList = new ArrayList<>(typeDesc.getFields());
            Collections.sort(fieldDescList, FieldDescComparator.INSTANCE);

            Schema schema = inferSchemaForType(typeDesc.getTypeName(), fieldDescList);
            augmentSchemaWithCustomMetaData(schema, referencedRecordTypeInfo, null);

            return schema;
        } catch (NetSuiteException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public List<String> getSearchFieldOperators() {
        List<SearchFieldOperatorName> operatorList =
                new ArrayList<>(metaDataSource.getBasicMetaData().getSearchOperatorNames());
        List<String> operatorNames = new ArrayList<>(operatorList.size());
        for (SearchFieldOperatorName operatorName : operatorList) {
            operatorNames.add(operatorName.getQualifiedName());
        }
        // Sort by name alphabetically
        Collections.sort(operatorNames);
        return operatorNames;
    }

    @Override
    public Schema getSchemaForUpdateFlow(String typeName, Schema schema) {
        RecordTypeInfo recordTypeInfo = metaDataSource.getRecordType(typeName);
        TypeDesc typeDesc = metaDataSource.getTypeInfo(typeName);

        List<FieldDesc> fieldDescList = new ArrayList<>();
        Schema.Field internalIdField = getNsFieldByName(schema, "internalId");
        if (internalIdField == null) {
            FieldDesc fieldDesc = typeDesc.getField("internalId");
            fieldDescList.add(fieldDesc);
        }
        Schema.Field externalIdField = getNsFieldByName(schema, "externalId");
        if (externalIdField == null) {
            FieldDesc fieldDesc = typeDesc.getField("externalId");
            fieldDescList.add(fieldDesc);
        }
        if (recordTypeInfo instanceof CustomRecordTypeInfo) {
            Schema.Field scriptIdField = getNsFieldByName(schema, "scriptId");
            if (scriptIdField == null) {
                FieldDesc fieldDesc = typeDesc.getField("scriptId");
                if (fieldDesc != null) {
                    fieldDescList.add(fieldDesc);
                }
            }
        }

        List<Schema.Field> fields = new ArrayList<>();

        Schema.Field f;

        if (!fieldDescList.isEmpty()) {
            Schema schemaToAdd = inferSchemaForType(typeName, fieldDescList);

            for (Schema.Field sourceField : schemaToAdd.getFields()) {
                f = copyField(sourceField);
                f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
                f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
                fields.add(f);
            }
        }

        return extendSchema(schema, typeName + "_FLOW", fields);
    }

    @Override
    public Schema getSchemaForDeleteFlow(String typeName, Schema schema) {
        RecordTypeInfo recordTypeInfo = metaDataSource.getRecordType(typeName);
        TypeDesc typeDesc = metaDataSource.getTypeInfo(typeName);

        List<FieldDesc> fieldDescList = new ArrayList<>();
        Schema.Field internalIdField = getNsFieldByName(schema, "internalId");
        if (internalIdField == null) {
            FieldDesc fieldDesc = typeDesc.getField("internalId");
            fieldDescList.add(fieldDesc);
        }
        Schema.Field externalIdField = getNsFieldByName(schema, "externalId");
        if (externalIdField == null) {
            FieldDesc fieldDesc = typeDesc.getField("externalId");
            fieldDescList.add(fieldDesc);
        }
        if (recordTypeInfo instanceof CustomRecordTypeInfo) {
            Schema.Field scriptIdField = getNsFieldByName(schema, "scriptId");
            if (scriptIdField == null) {
                FieldDesc fieldDesc = typeDesc.getField("scriptId");
                if (fieldDesc != null) {
                    fieldDescList.add(fieldDesc);
                }
            }
        }

        List<Schema.Field> fields = new ArrayList<>();

        Schema.Field f;

        if (!fieldDescList.isEmpty()) {
            Schema schemaToAdd = inferSchemaForType(typeName, fieldDescList);

            for (Schema.Field sourceField : schemaToAdd.getFields()) {
                f = copyField(sourceField);
                f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
                f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
                fields.add(f);
            }
        }

        return extendSchema(schema, typeName + "_FLOW", fields);
    }

    @Override
    public Schema getSchemaForUpdateReject(String typeName, Schema schema) {
        return getSchemaForReject(schema, typeName + "_REJECT");
    }

    @Override
    public Schema getSchemaForDeleteReject(String typeName, Schema schema) {
        return getSchemaForReject(schema, typeName + "_REJECT");
    }

    public Schema getSchemaForReject(Schema schema, String newSchemaName) {
        List<Schema.Field> fields = new ArrayList<>();

        Schema.Field f;

        f = new Schema.Field("errorCode", Schema.create(Schema.Type.STRING), null, (Object) null);
        f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        fields.add(f);

        f = new Schema.Field("errorMessage", Schema.create(Schema.Type.STRING), null, (Object) null);
        f.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
        f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        fields.add(f);

        return extendSchema(schema, newSchemaName, fields);
    }

    /**
     * Infers an Avro schema for the given type. This can be an expensive operation so the schema
     * should be cached where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param name name of a record.
     * @return the schema for data given from the object.
     */
    public static Schema inferSchemaForType(String name, List<FieldDesc> fieldDescList) {
        List<Schema.Field> fields = new ArrayList<>();

        for (FieldDesc fieldDesc : fieldDescList) {
            final String fieldName = fieldDesc.getName();
            final String avroFieldName = toInitialUpper(fieldName);

            Schema.Field avroField = new Schema.Field(avroFieldName,
                    inferSchemaForField(fieldDesc), null, (Object) null);

            // Add some Talend6 custom properties to the schema.

            Schema avroFieldSchema = AvroUtils.unwrapIfNullable(avroField.schema());

            avroField.addProp(DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME, fieldDesc.getName());

            if (AvroUtils.isSameType(avroFieldSchema, AvroUtils._string())) {
                if (fieldDesc.getLength() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(fieldDesc.getLength()));
                }
            }

            if (fieldDesc instanceof CustomFieldDesc) {
                CustomFieldDesc customFieldInfo = (CustomFieldDesc) fieldDesc;
                CustomFieldRefType customFieldRefType = customFieldInfo.getCustomFieldType();

                avroField.addProp(DiSchemaConstants.TALEND6_COLUMN_SOURCE_TYPE, customFieldRefType.getTypeName());

                if (customFieldRefType == CustomFieldRefType.DATE) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'");
                }

                NsRef ref = customFieldInfo.getRef();
                if (StringUtils.isNotEmpty(ref.getName())) {
                    avroField.addProp(DiSchemaConstants.TALEND6_COMMENT, ref.getName());
                }

            } else {
                Class<?> fieldType = fieldDesc.getValueType();

                avroField.addProp(DiSchemaConstants.TALEND6_COLUMN_SOURCE_TYPE, fieldType.getSimpleName());

                if (fieldType == XMLGregorianCalendar.class) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'");
                }
            }

            if (avroField.defaultVal() != null) {
                avroField.addProp(SchemaConstants.TALEND_COLUMN_DEFAULT, String.valueOf(avroField.defaultVal()));
            }

            if (fieldDesc.isKey()) {
                avroField.addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, Boolean.TRUE.toString());
            }

            fields.add(avroField);
        }

        return Schema.createRecord(name, null, null, false, fields);
    }

    /**
     * Infers an Avro schema for the given FieldDesc. This can be an expensive operation so the schema should be
     * cached where possible. The return type will be the Avro Schema that can contain the fieldDesc data without loss of
     * precision.
     *
     * @param fieldDesc the <code>FieldDesc</code> to analyse.
     * @return the schema for data that the fieldDesc describes.
     */
    public static Schema inferSchemaForField(FieldDesc fieldDesc) {
        Schema base;

        if (fieldDesc instanceof CustomFieldDesc) {
            CustomFieldDesc customFieldInfo = (CustomFieldDesc) fieldDesc;
            CustomFieldRefType customFieldRefType = customFieldInfo.getCustomFieldType();

            if (customFieldRefType == CustomFieldRefType.BOOLEAN) {
                base = AvroUtils._boolean();
            } else if (customFieldRefType == CustomFieldRefType.LONG) {
                base = AvroUtils._long();
            } else if (customFieldRefType == CustomFieldRefType.DOUBLE) {
                base = AvroUtils._double();
            } else if (customFieldRefType == CustomFieldRefType.DATE) {
                base = AvroUtils._logicalTimestamp();
            } else if (customFieldRefType == CustomFieldRefType.STRING) {
                base = AvroUtils._string();
            } else {
                base = AvroUtils._string();
            }

        } else {
            Class<?> fieldType = fieldDesc.getValueType();

            if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
                base = AvroUtils._boolean();
            } else if (fieldType == Integer.TYPE || fieldType == Integer.class) {
                base = AvroUtils._int();
            } else if (fieldType == Long.TYPE || fieldType == Long.class) {
                base = AvroUtils._long();
            } else if (fieldType == Float.TYPE || fieldType == Float.class) {
                base = AvroUtils._float();
            } else if (fieldType == Double.TYPE || fieldType == Double.class) {
                base = AvroUtils._double();
            } else if (fieldType == XMLGregorianCalendar.class) {
                base = AvroUtils._logicalTimestamp();
            } else if (fieldType == String.class) {
                base = AvroUtils._string();
            } else if (fieldType.isEnum()) {
                base = AvroUtils._string();
            } else {
                base = AvroUtils._string();
            }
        }

        base = fieldDesc.isNullable() ? AvroUtils.wrapAsNullable(base) : base;

        return base;
    }

    /**
     * Augment a given <code>Schema</code> with customization related meta data.
     *
     * @param schema schema to be augmented
     * @param recordTypeInfo information about record type to be used for augmentation
     * @param fieldDescList list of field descriptors to be used for augmentation
     */
    protected void augmentSchemaWithCustomMetaData(final Schema schema,
            final RecordTypeInfo recordTypeInfo, final Collection<FieldDesc> fieldDescList) {

        if (recordTypeInfo == null) {
            // Not a record, do nothing
            return;
        }

        // Add custom record type meta data to a key field
        if (recordTypeInfo instanceof CustomRecordTypeInfo) {
            CustomRecordTypeInfo customRecordTypeInfo = (CustomRecordTypeInfo) recordTypeInfo;
            for (Schema.Field field : schema.getFields()) {
                writeCustomRecord(metaDataSource.getBasicMetaData(), field, customRecordTypeInfo);
            }
        }

        // Add custom field meta data to fields
        if (fieldDescList != null && !fieldDescList.isEmpty()) {
            Map<String, CustomFieldDesc> customFieldDescMap = getCustomFieldDescMap(fieldDescList);
            if (!customFieldDescMap.isEmpty()) {
                for (Schema.Field field : schema.getFields()) {
                    String nsFieldName = getNsFieldName(field);
                    CustomFieldDesc customFieldDesc = customFieldDescMap.get(nsFieldName);
                    if (customFieldDesc != null) {
                        writeCustomField(field, customFieldDesc);
                    }
                }
            }
        }
    }

    public static Schema extendSchema(Schema sourceSchema, String newSchemaName, List<Schema.Field> fieldsToAdd) {
        Schema newSchema = Schema.createRecord(newSchemaName,
                sourceSchema.getDoc(), sourceSchema.getNamespace(),
                sourceSchema.isError());

        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : sourceSchema.getFields()) {
            Schema.Field field = copyField(se);
            copyFieldList.add(field);
        }

        copyFieldList.addAll(fieldsToAdd);

        newSchema.setFields(copyFieldList);

        for (Map.Entry<String, Object> entry : sourceSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

    public static Schema.Field copyField(final Schema.Field sourceField) {
        Schema.Field field = new Schema.Field(sourceField.name(), sourceField.schema(),
                sourceField.doc(), sourceField.defaultVal(), sourceField.order());
        field.getObjectProps().putAll(sourceField.getObjectProps());
        for (Map.Entry<String, Object> entry : sourceField.getObjectProps().entrySet()) {
            field.addProp(entry.getKey(), entry.getValue());
        }
        return field;
    }

    /**
     * Write custom record meta data to a given <code>JsonProperties</code>.
     *
     * @see NetSuiteSchemaConstants
     *
     * @param basicMetaData basic meta data
     * @param properties properties object which to write meta data to
     * @param recordTypeInfo information about record type to be used
     */
    public static void writeCustomRecord(BasicMetaData basicMetaData, JsonProperties properties, CustomRecordTypeInfo recordTypeInfo) {
        NsRef ref = recordTypeInfo.getRef();
        RecordTypeDesc recordTypeDesc = recordTypeInfo.getRecordType();

        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD, "true");
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_SCRIPT_ID, ref.getScriptId());
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_INTERNAL_ID, ref.getInternalId());
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_CUSTOMIZATION_TYPE, ref.getType());
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_TYPE, recordTypeDesc.getType());
    }

    /**
     * Read custom record meta data from a given <code>JsonProperties</code>.
     *
     * @see NetSuiteSchemaConstants
     *
     * @param basicMetaData basic meta data
     * @param properties properties object which to read meta data from
     * @return custom record type info or <code>null</code> if meta data was not found
     */
    public static CustomRecordTypeInfo readCustomRecord(BasicMetaData basicMetaData, JsonProperties properties) {
        String scriptId = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_SCRIPT_ID);
        if (StringUtils.isEmpty(scriptId)) {
            return null;
        }
        String internalId = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_INTERNAL_ID);
        String customizationType = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_CUSTOMIZATION_TYPE);
        String recordType = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_RECORD_TYPE);

        NsRef ref = new NsRef();
        ref.setRefType(RefType.CUSTOMIZATION_REF);
        ref.setScriptId(scriptId);
        ref.setInternalId(internalId);
        ref.setType(customizationType);

        RecordTypeDesc recordTypeDesc = basicMetaData.getRecordType(recordType);
        CustomRecordTypeInfo recordTypeInfo = new CustomRecordTypeInfo(scriptId, recordTypeDesc, ref);

        return recordTypeInfo;
    }

    /**
     * Write custom field meta data to a given <code>JsonProperties</code>.
     *
     * @see NetSuiteSchemaConstants
     *
     * @param properties properties object which to write meta data to
     * @param fieldDesc information about custom field to be used
     */
    public static void writeCustomField(JsonProperties properties, CustomFieldDesc fieldDesc) {
        NsRef ref = fieldDesc.getRef();
        CustomFieldRefType customFieldRefType = fieldDesc.getCustomFieldType();

        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD, "true");
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_SCRIPT_ID, ref.getScriptId());
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_INTERNAL_ID, ref.getInternalId());
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_CUSTOMIZATION_TYPE, ref.getType());
        properties.addProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_TYPE, customFieldRefType.name());
    }

    /**
     * Read custom field meta data from a given <code>JsonProperties</code>.
     *
     * @see NetSuiteSchemaConstants
     *
     * @param properties properties object which to read meta data from
     * @return custom field info or <code>null</code> if meta data was not found
     */
    public static CustomFieldDesc readCustomField(JsonProperties properties) {
        String scriptId = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_SCRIPT_ID);
        if (StringUtils.isEmpty(scriptId)) {
            return null;
        }
        String internalId = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_INTERNAL_ID);
        String customizationType = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_CUSTOMIZATION_TYPE);
        String type = properties.getProp(NetSuiteSchemaConstants.NS_CUSTOM_FIELD_TYPE);

        NsRef ref = new NsRef();
        ref.setRefType(RefType.CUSTOMIZATION_REF);
        ref.setScriptId(scriptId);
        ref.setInternalId(internalId);
        ref.setType(customizationType);

        CustomFieldRefType customFieldRefType = CustomFieldRefType.valueOf(type);

        CustomFieldDesc fieldDesc = new CustomFieldDesc();
        fieldDesc.setCustomFieldType(customFieldRefType);
        fieldDesc.setRef(ref);
        fieldDesc.setName(scriptId);
        fieldDesc.setValueType(getCustomFieldValueClass(customFieldRefType));
        fieldDesc.setNullable(true);

        return fieldDesc;
    }

    /**
     * Build and return map of custom field descriptors.
     *
     * @param fieldDescList list of custom field descriptors
     * @return map of custom field descriptors by names
     */
    public static Map<String, CustomFieldDesc> getCustomFieldDescMap(Collection<FieldDesc> fieldDescList) {
        Map<String, CustomFieldDesc> customFieldDescMap = new HashMap<>();
        for (FieldDesc fieldDesc : fieldDescList) {
            if (fieldDesc instanceof CustomFieldDesc) {
                CustomFieldDesc customFieldDesc = fieldDesc.asCustom();
                customFieldDescMap.put(customFieldDesc.getName(), customFieldDesc);
            }
        }
        return customFieldDescMap;
    }

    /**
     * Return type of value hold by a custom field.
     *
     * @param fieldDesc custom field descriptor
     * @return type of value
     */
    public static Class<?> getCustomFieldValueClass(CustomFieldDesc fieldDesc) {
        return getCustomFieldValueClass(fieldDesc.getCustomFieldType());
    }

    /**
     * Return type of value hold by a custom field with given <code>CustomFieldRefType</code>.
     *
     * @param customFieldRefType type of field
     * @return type of value
     */
    public static Class<?> getCustomFieldValueClass(CustomFieldRefType customFieldRefType) {
        Class<?> valueClass = null;
        switch (customFieldRefType) {
        case BOOLEAN:
            valueClass = Boolean.class;
            break;
        case STRING:
            valueClass = String.class;
            break;
        case LONG:
            valueClass = Long.class;
            break;
        case DOUBLE:
            valueClass = Double.class;
            break;
        case DATE:
            valueClass = XMLGregorianCalendar.class;
            break;
        case SELECT:
        case MULTI_SELECT:
            valueClass = String.class;
            break;
        }
        return valueClass;
    }

    /**
     * Return internal (NetSuite specific) name for a given <code>schema field</code>.
     *
     * @param field schema field
     * @return name
     */
    public static String getNsFieldName(Schema.Field field) {
        String name = field.getProp(DiSchemaConstants.TALEND6_COLUMN_ORIGINAL_DB_COLUMN_NAME);
        return name != null ? toInitialLower(name) : toInitialLower(field.name());
    }

    /**
     * Find and return <code>schema field</code> by it's name.
     *
     * @param schema schema
     * @param fieldName name of field to be found
     * @return schema field or <code>null</code> if field was not found
     */
    public static Schema.Field getNsFieldByName(Schema schema, String fieldName) {
        for (Schema.Field field : schema.getFields()) {
            String nsFieldName = getNsFieldName(field);
            if (fieldName.equals(nsFieldName)) {
                return field;
            }
        }
        return null;
    }

    public static class FieldDescComparator implements Comparator<FieldDesc> {

        public static final FieldDescComparator INSTANCE = new FieldDescComparator();

        @Override
        public int compare(FieldDesc o1, FieldDesc o2) {
            int result = Boolean.compare(o1.isKey(), o2.isKey());
            if (result != 0) {
                return result * -1;
            }
            result = o1.getName().compareTo(o2.getName());
            return result;
        }

    }
}
