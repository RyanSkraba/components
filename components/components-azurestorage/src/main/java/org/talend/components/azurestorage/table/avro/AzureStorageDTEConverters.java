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

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.microsoft.azure.storage.table.DynamicTableEntity;

/**
 * Group of Azure Dynamic Table Entity converters that convert Azure {@link DynamicTableEntity} type/values to Avro
 * style
 */
public class AzureStorageDTEConverters {

    public static final String COL_PARITION_KEY = "PartitionKey";

    public static final String COL_ROW_KEY = "RowKey";

    public static final String COL_TIMESTAMP = "Timestamp";

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageDTEConverters.class);

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageDTEConverters.class);

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

    public class StringDTEConverter extends DTEConverter {

        Field f;

        String mappedName;

        public StringDTEConverter(Field f, String mappedName) {
            this.f = f;
            this.mappedName = mappedName;
        }

        @Override
        public Object convertToAvro(DynamicTableEntity value) {
            try {

                if (COL_PARITION_KEY.equals(f.name()) || COL_PARITION_KEY.equals(mappedName)) {
                    return value.getPartitionKey();
                }

                if (COL_ROW_KEY.equals(f.name()) || COL_ROW_KEY.equals(mappedName)) {
                    return value.getRowKey();
                }

                if (f.name().equals(COL_TIMESTAMP) || mappedName.equals(COL_TIMESTAMP)) { // should be set to DT
                                                                                          // but...
                    String pattern = f.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
                    if (pattern != null && !pattern.isEmpty())
                        return new SimpleDateFormat(pattern).format(value.getTimestamp());
                    else
                        return value.getTimestamp();
                }

                if (!value.getProperties().containsKey(mappedName) || value.getProperties().get(mappedName) == null) {
                    return null;
                }

                return value.getProperties().get(mappedName).getValueAsString();

            } catch (Exception e) {
                LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                throw new ComponentException(e);
            }
        }
    }

    public class IntegerDTEConverter extends DTEConverter {

        String mappedName;

        public IntegerDTEConverter(String mappedName) {
            this.mappedName = mappedName;
        }

        @Override
        public Object convertToAvro(DynamicTableEntity value) {
            try {

                if (!value.getProperties().containsKey(mappedName) || value.getProperties().get(mappedName) == null) {
                    return null;
                }

                return value.getProperties().get(mappedName).getValueAsInteger();
            } catch (Exception e) {
                LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                throw new ComponentException(e);
            }
        }
    }

    public class DateDTEConverter extends DTEConverter {

        Field f;

        String mappedName;

        public DateDTEConverter(Field f, String mappedName) {
            this.f = f;
            this.mappedName = mappedName;
        }

        @Override
        public Object convertToAvro(DynamicTableEntity value) {
            try {
                if (COL_TIMESTAMP.equals(f.name()) || COL_TIMESTAMP.equals(mappedName)) {
                    return value.getTimestamp();
                }

                if (!value.getProperties().containsKey(mappedName) || value.getProperties().get(mappedName) == null) {
                    return null;
                }

                return value.getProperties().get(mappedName).getValueAsDate();
            } catch (Exception e) {
                LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                throw new ComponentException(e);
            }
        }
    }

    public class DoubleDTEConverter extends DTEConverter {

        String mappedName;

        public DoubleDTEConverter(String mappedName) {
            this.mappedName = mappedName;
        }

        @Override
        public Object convertToAvro(DynamicTableEntity value) {
            try {

                if (!value.getProperties().containsKey(mappedName) || value.getProperties().get(mappedName) == null) {
                    return null;
                }

                return value.getProperties().get(mappedName).getValueAsDouble();
            } catch (Exception e) {
                LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                throw new ComponentException(e);
            }
        }
    }

    public class LongDTEConverter extends DTEConverter {

        Field f;

        String mappedName;

        public LongDTEConverter(Field f, String mappedName) {
            this.f = f;
            this.mappedName = mappedName;
        }

        @Override
        public Object convertToAvro(DynamicTableEntity value) {
            try {
                if (COL_TIMESTAMP.equals(f.name()) || COL_TIMESTAMP.equals(mappedName)) {
                    return value.getTimestamp();
                }

                if (!value.getProperties().containsKey(mappedName) || value.getProperties().get(mappedName) == null) {
                    return null;
                }

                return value.getProperties().get(mappedName).getValueAsLong();

            } catch (Exception e) {
                LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                throw new ComponentException(e);
            }
        }
    }

    public class BooleanDTEConverter extends DTEConverter {

        String mappedName;

        public BooleanDTEConverter(String mappedName) {
            this.mappedName = mappedName;
        }

        @Override
        public Object convertToAvro(DynamicTableEntity value) {
            try {
                if (!value.getProperties().containsKey(mappedName)) {
                    return null;
                }

                return value.getProperties().get(mappedName).getValueAsBoolean();
            } catch (Exception e) {
                LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                throw new ComponentException(e);
            }
        }
    }

    public class ByteArrayDTEConverter extends DTEConverter {

        String mappedName;

        public ByteArrayDTEConverter(String mappedName) {
            this.mappedName = mappedName;
        }

        @Override
        public Object convertToAvro(DynamicTableEntity value) {
            try {

                if (!value.getProperties().containsKey(mappedName) || value.getProperties().get(mappedName) == null) {
                    return null;
                }

                return value.getProperties().get(mappedName).getValueAsByteArray();
            } catch (Exception e) {
                LOGGER.error(i18nMessages.getMessage("error.ConversionError", e));
                throw new ComponentException(e);
            }
        }
    }

}
