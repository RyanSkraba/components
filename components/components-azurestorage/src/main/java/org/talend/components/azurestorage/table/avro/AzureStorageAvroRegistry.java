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
package org.talend.components.azurestorage.table.avro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.java8.SerializableFunction;

import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EntityProperty;

/**
 * Class AzureStorageAvroRegistry.
 */
public class AzureStorageAvroRegistry extends AvroRegistry {

    public static final String COL_PARITION_KEY = "PartitionKey";

    public static final String COL_ROW_KEY = "RowKey";

    public static final String COL_TIMESTAMP = "Timestamp";

    /** instance - instance. */
    private static AzureStorageAvroRegistry instance;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageAvroRegistry.class);
    
    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageAvroRegistry.class);

    /**
     * Instantiates a new AzureStorageAvroRegistry().
     */
    protected AzureStorageAvroRegistry() {

        registerSchemaInferrer(DynamicTableEntity.class, new SerializableFunction<DynamicTableEntity, Schema>() {

            private static final long serialVersionUID = 6026115557764489022L;

            @Override
            public Schema apply(DynamicTableEntity t) {
                try {
                    return inferSchemaDynamicTableEntity(t);
                } catch (Exception e) {
                    throw new ComponentException(e);
                }
            }
        });
    }

    /**
     * get singleton instance.
     *
     * @return {@link AzureStorageAvroRegistry} azure storage avro registry
     */
    public static AzureStorageAvroRegistry get() {
        if (instance == null)
            instance = new AzureStorageAvroRegistry();
        return instance;
    }

    protected Schema inferSchemaDynamicTableEntity(DynamicTableEntity entity) {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("PartitionKey", AvroUtils._string(), null, (Object) null));
        fields.add(new Field("RowKey", AvroUtils._string(), null, (Object) null));
        fields.add(new Field("Timestamp", AvroUtils._date(), null, (Object) null));

        // FIXME set tableName properly and manage nameMappings
        String tableName = "schemaInfered";
        for (Entry<String, EntityProperty> f : entity.getProperties().entrySet()) {
            String fieldName = f.getKey();
            Field field = getAvroMapping(fieldName, f.getValue());
            fields.add(field);
        }
        return Schema.createRecord(tableName, null, null, false, fields);
    }

    /**
     * getAvroMapping.
     *
     * @param name {@link String} name
     * @param property {@link EntityProperty} property
     * @return {@link Field} field
     */
    protected Field getAvroMapping(String name, EntityProperty property) {
        Field field = null;
        switch (property.getEdmType()) {
        /** <strong>Edm.Binary</strong> Represents fixed- or variable-length binary data */
        case BINARY:
            field = new Field(name, AvroUtils._bytes(), null, (Object) null);
            break;
        /** <strong>Edm.Byte</strong> Represents a unsigned 8-bit integer value */
        case BYTE:
            /** <strong>Edm.SByte</strong> Represents a signed 8-bit integer value */
        case SBYTE:
            field = new Field(name, AvroUtils._byte(), null, (Object) null);
            break;
        /** <strong>Edm.Boolean</strong> Represents the mathematical concept of binary-valued logic */
        case BOOLEAN:
            field = new Field(name, AvroUtils._boolean(), null, (Object) null);
            break;
        /**
         * <strong>Edm.DateTime</strong> Represents date and time with values ranging from 12:00:00 midnight, January 1,
         * 1753 A.D. through 11:59:59 P.M, December 9999 A.D.
         */
        case DATE_TIME:
            field = new Field(name, AvroUtils._date(), null, (Object) null);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'");
            break;
        /**
         * <strong>Edm.DateTimeOffset</strong> Represents date and time as an Offset in minutes from GMT, with values
         * ranging from 12:00:00 midnight, January 1, 1753 A.D. through 11:59:59 P.M, December 9999 A.D
         */
        case DATE_TIME_OFFSET:
            field = new Field(name, AvroUtils._date(), null, (Object) null);
            break;
        /**
         * <strong>Edm.Time</strong> Represents the time of day with values ranging from 0:00:00.x to 23:59:59.y, where
         * x and y depend upon the precision
         */
        case TIME:
            field = new Field(name, AvroUtils._logicalTime(), null, (Object) null);
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss.SSS'Z'");
            break;
        /**
         * <strong>Edm.Decimal</strong> Represents numeric values with fixed precision and scale. This type can describe
         * a numeric value ranging from negative 10^255 + 1 to positive 10^255 -1
         */
        case DECIMAL:
            field = new Field(name, AvroUtils._decimal(), null, (Object) null);
            break;
        /**
         * <strong>Edm.Double</strong> Represents a floating point number with 15 digits precision that can represent
         * values with approximate range of +/- 2.23e -308 through +/- 1.79e +308
         */
        case DOUBLE:
            field = new Field(name, AvroUtils._double(), null, (Object) null);
            break;
        /** <strong>Edm.Int16</strong>Represents a signed 16-bit integer value */
        case INT16:
            field = new Field(name, AvroUtils._short(), null, (Object) null);
            break;
        /** <strong>Edm.Int32</strong> Represents a signed 32-bit integer value */
        case INT32:
            field = new Field(name, AvroUtils._int(), null, (Object) null);
            break;
        /** <strong>Edm.Guid</strong> Represents a 16-byte (128-bit) unique identifier value */
        case GUID:
            /** <strong>Edm.Int64</strong> Represents a signed 64-bit integer value */
        case INT64:
            field = new Field(name, AvroUtils._long(), null, (Object) null);
            break;
        /**
         * <strong>Edm.Single</strong> Represents a floating point number with 7 digits precision that can represent
         * values with approximate range of +/- 1.18e -38 through +/- 3.40e +38
         */
        case SINGLE:
            field = new Field(name, AvroUtils._float(), null, (Object) null);
            break;
        /** <strong>Null</strong> Represents the absence of a value */
        case NULL:
            /** <strong>Edm.String</strong> Represents fixed- or variable-length character data */
        case STRING:
        default:
            field = new Field(name, AvroUtils._string(), null, (Object) null);
        }
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, name);
        field.addProp(SchemaConstants.JAVA_CLASS_FLAG, property.getType().getName());

        return field;
    }

    public DTEConverter getConverter(final Field f, final String mappedName) {
        Schema basicSchema = AvroUtils.unwrapIfNullable(f.schema());

        if (AvroUtils.isSameType(basicSchema, AvroUtils._string())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        if (f.name().equals(COL_PARITION_KEY) || mappedName.equals(COL_PARITION_KEY))
                            return value.getPartitionKey();
                        if (f.name().equals(COL_ROW_KEY) || mappedName.equals(COL_ROW_KEY))
                            return value.getRowKey();
                        if (f.name().equals(COL_TIMESTAMP) || mappedName.equals(COL_TIMESTAMP)) { // should be set to DT
                                                                                                  // but...
                            String pattern = f.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
                            if (pattern != null && !pattern.isEmpty())
                                return new SimpleDateFormat(pattern).format(value.getTimestamp());
                            else
                                return value.getTimestamp();
                        }
                        return value.getProperties().get(mappedName).getValueAsString();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._int()) || AvroUtils.isSameType(basicSchema, AvroUtils._short())
                || AvroUtils.isSameType(basicSchema, AvroUtils._byte())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        return value.getProperties().get(mappedName).getValueAsInteger();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._date())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        if (f.name().equals(COL_TIMESTAMP) || mappedName.equals(COL_TIMESTAMP))
                            return value.getTimestamp();
                        return value.getProperties().get(mappedName).getValueAsDate();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._decimal())
                || AvroUtils.isSameType(basicSchema, AvroUtils._double())
                || AvroUtils.isSameType(basicSchema, AvroUtils._float())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        return value.getProperties().get(mappedName).getValueAsDouble();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._long())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        if (f.name().equals(COL_TIMESTAMP) || mappedName.equals(COL_TIMESTAMP))
                            return value.getTimestamp();
                        return value.getProperties().get(mappedName).getValueAsLong();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._boolean())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        return value.getProperties().get(mappedName).getValueAsBoolean();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._character())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        return value.getProperties().get(mappedName).getValueAsString();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._bytes())) {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        return value.getProperties().get(mappedName).getValueAsByteArray();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        } else {
            return new DTEConverter() {

                @Override
                public Object convertToAvro(DynamicTableEntity value) {
                    try {
                        return value.getProperties().get(mappedName).getValueAsString();
                    } catch (Exception e) {
                        LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                        throw new ComponentException(e);
                    }
                }
            };
        }
    }

    public abstract class DTEConverter implements AvroConverter<DynamicTableEntity, Object> {

        @Override
        public Schema getSchema() {
            return null;
        }

        @Override
        public Class<DynamicTableEntity> getDatumClass() {
            return null;
        }

        @Override
        public DynamicTableEntity convertToDatum(Object value) {
            return null;
        }

    }
}
