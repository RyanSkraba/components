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

import static org.talend.components.netsuite.client.model.beans.Beans.getEnumAccessor;
import static org.talend.components.netsuite.client.model.beans.Beans.getProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.getSimpleProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.Schema;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.client.model.beans.EnumAccessor;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.daikon.di.DiSchemaConstants;

/**
 *
 */
public abstract class NsObjectTransducer {

    protected NetSuiteClientService<?> clientService;

    protected final DatatypeFactory datatypeFactory;

    protected Map<Class<?>, ValueConverter<?, ?>> valueConverterCache = new HashMap<>();

    public NsObjectTransducer(NetSuiteClientService<?> clientService) {
        this.clientService = clientService;

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new ComponentException(e);
        }
    }

    public NetSuiteClientService<?> getClientService() {
        return clientService;
    }

    public DatatypeFactory getDatatypeFactory() {
        return datatypeFactory;
    }

    protected Schema getDynamicSchema(TypeDesc typeDesc, Schema designSchema, String targetSchemaName) {
        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();

        String dynamicPosProp = designSchema.getProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION);
        List<Schema.Field> fields = new ArrayList<>();

        if (dynamicPosProp != null) {
            Set<String> designFieldNames = new HashSet<>(designSchema.getFields().size());
            for (Schema.Field field : designSchema.getFields()) {
                String fieldName = field.name();
                designFieldNames.add(fieldName);
            }

            int dynPos = Integer.parseInt(dynamicPosProp);
            int dynamicColumnSize = fieldMap.size() - designSchema.getFields().size();

            List<FieldDesc> dynaFieldDescList = new ArrayList<>(dynamicColumnSize);
            for (FieldDesc fieldDesc : fieldMap.values()) {
                String fieldName = fieldDesc.getName();
                if (!designFieldNames.contains(fieldName)) {
                    dynaFieldDescList.add(fieldDesc);
                }
            }

            if (designSchema.getFields().size() > 0) {
                for (Schema.Field field : designSchema.getFields()) {
                    // Dynamic column is first or middle column in design schema
                    if (dynPos == field.pos()) {
                        for (int i = 0; i < dynamicColumnSize; i++) {
                            // Add dynamic schema fields
                            FieldDesc fieldDesc = dynaFieldDescList.get(i);
                            fields.add(createSchemaField(fieldDesc));
                        }
                    }

                    // Add fields of design schema
                    Schema.Field avroField = new Schema.Field(field.name(), field.schema(), null, field.defaultVal());
                    Map<String, Object> fieldProps = field.getObjectProps();
                    for (String propName : fieldProps.keySet()) {
                        Object propValue = fieldProps.get(propName);
                        if (propValue != null) {
                            avroField.addProp(propName, propValue);
                        }
                    }

                    fields.add(avroField);

                    // Dynamic column is last column in design schema
                    if (field.pos() == (designSchema.getFields().size() - 1) && dynPos == (field.pos() + 1)) {
                        for (int i = 0; i < dynamicColumnSize; i++) {
                            // Add dynamic schema fields
                            FieldDesc fieldDesc = dynaFieldDescList.get(i);
                            fields.add(createSchemaField(fieldDesc));
                        }
                    }
                }
            } else {
                // All fields are included in dynamic schema
                for (String fieldName : fieldMap.keySet()) {
                    FieldDesc fieldDesc = fieldMap.get(fieldName);
                    fields.add(createSchemaField(fieldDesc));
                }
            }
        } else {
            // All fields are included in dynamic schema
            for (String fieldName : fieldMap.keySet()) {
                FieldDesc fieldDesc = fieldMap.get(fieldName);
                fields.add(createSchemaField(fieldDesc));
            }
        }

        Schema schema = Schema.createRecord(targetSchemaName, null, null, false, fields);
        return schema;
    }

    protected Schema.Field createSchemaField(FieldDesc fieldDesc) {
        Schema avroFieldType = NetSuiteDatasetRuntimeImpl.inferSchemaForField(fieldDesc);
        Schema.Field avroField = new Schema.Field(fieldDesc.getName(), avroFieldType, null, null);
        return avroField;
    }

    protected Map<String, Object> getMapView(Object nsObject, Schema schema, TypeDesc typeDesc) {
        BeanInfo beanInfo = Beans.getBeanInfo(typeDesc.getTypeClass());

        Map<String, Object> valueMap = new HashMap<>();

        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();

        Map<String, CustomFieldDesc> customFieldMap = new HashMap<>();
        for (Schema.Field field : schema.getFields()) {
            String fieldName = field.name();
            FieldDesc fieldDesc = fieldMap.get(fieldName);

            if (fieldDesc == null) {
                continue;
            }

            if (fieldDesc instanceof CustomFieldDesc) {
                customFieldMap.put(fieldName, (CustomFieldDesc) fieldDesc);
            } else {
                Object value = getSimpleProperty(nsObject, fieldDesc.getInternalName());
                valueMap.put(fieldName, value);
            }
        }

        if (!customFieldMap.isEmpty() &&
                beanInfo.getProperty("customFieldList") != null) {
            List<?> customFieldList = (List<?>) getProperty(nsObject, "customFieldList.customField");
            if (customFieldList != null && !customFieldList.isEmpty()) {
                for (Object customField : customFieldList) {
                    String scriptId = (String) getSimpleProperty(customField, "scriptId");
                    CustomFieldDesc customFieldInfo = customFieldMap.get(scriptId);
                    String fieldName = customFieldInfo.getName();
                    if (customFieldInfo != null) {
                        valueMap.put(fieldName, customField);
                    }
                }
            }
        }

        return valueMap;
    }

    protected Object readField(Map<String, Object> valueMap, FieldDesc fieldDesc) {
        String fieldName = fieldDesc.getName();
        ValueConverter valueConverter = getValueConverter(fieldDesc);
        if (fieldDesc instanceof CustomFieldDesc) {
            Object customField = valueMap.get(fieldName);
            if (customField != null) {
                Object value = getSimpleProperty(customField, "value");
                return valueConverter.convertInput(value);
            }
            return null;
        } else {
            Object value = valueMap.get(fieldName);
            return valueConverter.convertInput(value);
        }
    }

    protected Object writeField(Object nsObject, FieldDesc fieldDesc, Object value) {
        ValueConverter valueConverter = getValueConverter(fieldDesc);
        if (fieldDesc instanceof CustomFieldDesc) {
            CustomFieldDesc customFieldInfo = fieldDesc.asCustom();
            Object targetValue = valueConverter.convertOutput(value);

            if (targetValue != null) {
                CustomFieldRefType customFieldRefType = customFieldInfo.getCustomFieldType();

                Object customFieldListWrapper = getSimpleProperty(nsObject, "customFieldList");
                if (customFieldListWrapper == null) {
                    customFieldListWrapper = clientService.getBasicMetaData()
                            .createInstance("CustomFieldList");
                    setSimpleProperty(nsObject, "customFieldList", customFieldListWrapper);
                }
                List<Object> customFieldList = (List<Object>) getSimpleProperty(
                        customFieldListWrapper, "customField");

                Object customField = clientService.getBasicMetaData()
                        .createInstance(customFieldRefType.getTypeName());
                setSimpleProperty(customField, "scriptId", customFieldInfo.getRef().getScriptId());
                setSimpleProperty(customField, "internalId", customFieldInfo.getRef().getInternalId());

                setSimpleProperty(customField, "value", targetValue);

                customFieldList.add(customField);

                return targetValue;
            }

        } else {
            Object targetValue = valueConverter.convertOutput(value);

            if (targetValue != null) {
                setSimpleProperty(nsObject, fieldDesc.getInternalName(), targetValue);
                return targetValue;
            }
        }

        return null;
    }

    public ValueConverter<?, ?> getValueConverter(FieldDesc fieldDesc) {
        Class<?> valueClass = null;

        if (fieldDesc instanceof CustomFieldDesc) {
            CustomFieldDesc customFieldInfo = (CustomFieldDesc) fieldDesc;
            CustomFieldRefType customFieldRefType = customFieldInfo.getCustomFieldType();

            switch (customFieldRefType) {
            case BOOLEAN:
                valueClass = Boolean.TYPE;
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
            }
        } else {
            valueClass = fieldDesc.getValueType();
        }

        ValueConverter<?, ?> converter = null;
        if (valueClass != null) {
            converter = getValueConverter(valueClass);
        }
        if (converter == null) {
            converter = NullValueConverter.INSTANCE;
        }
        return converter;
    }

    protected ValueConverter<?, ?> getValueConverter(Class<?> valueClass) {
        ValueConverter<?, ?> converter = valueConverterCache.get(valueClass);
        if (converter == null) {
            converter = createValueConverter(valueClass);
            if (converter != null) {
                valueConverterCache.put(valueClass, converter);
            }
        }
        return converter;
    }

    protected ValueConverter<?, ?> createValueConverter(Class<?> valueClass) {
        if (valueClass == Boolean.TYPE || valueClass == Boolean.class ||
                valueClass == Integer.TYPE || valueClass == Integer.class ||
                valueClass == Long.TYPE || valueClass == Long.class ||
                valueClass == Double.TYPE || valueClass == Double.class ||
                valueClass == String.class) {
            return IdentityValueConverter.INSTANCE;
        } else if (valueClass == XMLGregorianCalendar.class) {
            return new XMLGregorianCalendarValueConverter(datatypeFactory);
        } else if (valueClass.isEnum()) {
            Class<Enum> enumClass = (Class<Enum>) valueClass;
            return new EnumValueConverter<>(enumClass, getEnumAccessor(enumClass));
        }
        return null;
    }

    public interface ValueConverter<T, U> {

        U convertInput(T value);

        T convertOutput(U value);
    }

    public static class NullValueConverter<T> implements ValueConverter<T, T> {

        public static final NullValueConverter INSTANCE = new NullValueConverter();

        @Override
        public T convertInput(T value) {
            return null;
        }

        @Override
        public T convertOutput(T value) {
            return null;
        }
    }

    public static class IdentityValueConverter<T> implements ValueConverter<T, T> {

        public static final IdentityValueConverter INSTANCE = new IdentityValueConverter();

        @Override
        public T convertInput(T value) {
            return value;
        }

        @Override
        public T convertOutput(T value) {
            return value;
        }
    }

    public static class EnumValueConverter<T extends Enum<T>> implements ValueConverter<T, String> {

        private final Class<T> clazz;
        private final EnumAccessor enumAccessor;

        public EnumValueConverter(Class<T> clazz, EnumAccessor enumAccessor) {
            this.clazz = clazz;
            this.enumAccessor = enumAccessor;
        }

        @Override
        public T convertOutput(String value) {
            if (value == null) {
                return null;
            }
            try {
                return (T) enumAccessor.getEnumValue(value);
            } catch (IllegalArgumentException ex) {
                // Fallback to .valueOf(String)
                return Enum.valueOf(clazz, value);
            }
        }

        @Override
        public String convertInput(Enum enumValue) {
            if (enumValue == null) {
                return null;
            }
            try {
                return enumAccessor.getStringValue(enumValue);
            } catch (IllegalArgumentException ex) {
                // Fallback to .name()
                return enumValue.name();
            }
        }
    }

    public static class XMLGregorianCalendarValueConverter implements ValueConverter<XMLGregorianCalendar, Long> {
        private DatatypeFactory datatypeFactory;

        public XMLGregorianCalendarValueConverter(DatatypeFactory datatypeFactory) {
            this.datatypeFactory = datatypeFactory;
        }

        @Override
        public XMLGregorianCalendar convertOutput(Long timestamp) {
            if (timestamp == null) {
                return null;
            }

            MutableDateTime dateTime = new MutableDateTime();
            dateTime.setMillis(timestamp);

            XMLGregorianCalendar xts = datatypeFactory.newXMLGregorianCalendar();
            xts.setYear(dateTime.getYear());
            xts.setMonth(dateTime.getMonthOfYear());
            xts.setDay(dateTime.getDayOfMonth());
            xts.setHour(dateTime.getHourOfDay());
            xts.setMinute(dateTime.getMinuteOfHour());
            xts.setSecond(dateTime.getSecondOfMinute());
            xts.setMillisecond(dateTime.getMillisOfSecond());
            xts.setTimezone(dateTime.getZone().toTimeZone().getOffset(dateTime.getMillis()) / 60000);

            return xts;
        }

        @Override
        public Long convertInput(XMLGregorianCalendar xts) {
            if (xts == null) {
                return null;
            }

            MutableDateTime dateTime = new MutableDateTime();
            try {
                dateTime.setYear(xts.getYear());
                dateTime.setMonthOfYear(xts.getMonth());
                dateTime.setDayOfMonth(xts.getDay());
                dateTime.setHourOfDay(xts.getHour());
                dateTime.setMinuteOfHour(xts.getMinute());
                dateTime.setSecondOfMinute(xts.getSecond());
                dateTime.setMillisOfSecond(xts.getMillisecond());

                DateTimeZone tz = DateTimeZone.forOffsetMillis(xts.getTimezone() * 60000);
                if (tz != null) {
                    dateTime.setZoneRetainFields(tz);
                }

                return dateTime.getMillis();
            } catch (IllegalArgumentException e) {
                throw new ComponentException(e);
            }
        }
    }

}

