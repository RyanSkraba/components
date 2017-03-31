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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.SimpleFieldDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.client.model.beans.EnumAccessor;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.daikon.di.DiSchemaConstants;
import org.talend.daikon.exception.ExceptionContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 *
 */
public abstract class NsObjectTransducer {
    public static final String JSON_NS_TYPE_PROPERTY_NAME = "nsType";

    protected NetSuiteClientService<?> clientService;
    protected MetaDataSource metaDataSource;

    protected final DatatypeFactory datatypeFactory;

    protected final ObjectMapper objectMapper;

    protected Map<Class<?>, ValueConverter<?, ?>> valueConverterCache = new HashMap<>();

    public NsObjectTransducer(NetSuiteClientService<?> clientService) {
        this.clientService = clientService;

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new ComponentException(e);
        }

        objectMapper = new ObjectMapper();

        objectMapper.setDefaultTyping(new NsTypeResolverBuilder(clientService.getBasicMetaData()));

        // Register JAXB annotation module to perform mapping of data model objects to/from JSON.
        JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
        objectMapper.registerModule(jaxbAnnotationModule);

        setMetaDataSource(clientService.getMetaDataSource());
    }

    public MetaDataSource getMetaDataSource() {
        return metaDataSource;
    }

    public void setMetaDataSource(MetaDataSource metaDataSource) {
        this.metaDataSource = metaDataSource;
    }

    public NetSuiteClientService<?> getClientService() {
        return clientService;
    }

    public DatatypeFactory getDatatypeFactory() {
        return datatypeFactory;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected Schema getDynamicSchema(TypeDesc typeDesc, Schema designSchema, String targetSchemaName) {
        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();

        String dynamicPosProp = designSchema.getProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION);
        List<Schema.Field> fields = new ArrayList<>();

        if (dynamicPosProp != null) {
            Set<String> designFieldNames = new HashSet<>(designSchema.getFields().size());
            for (Schema.Field field : designSchema.getFields()) {
                String fieldName = NetSuiteDatasetRuntimeImpl.getNsFieldName(field);
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
                    Schema.Field avroField = new Schema.Field(
                            field.name(), field.schema(), null, field.defaultVal());
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
        Schema.Field avroField = new Schema.Field(fieldDesc.getName(), avroFieldType, null, (Object) null);
        return avroField;
    }

    protected Map<String, Object> getMapView(Object nsObject, Schema schema, TypeDesc typeDesc) {
        Map<String, Object> valueMap = new HashMap<>();

        BeanInfo beanInfo = Beans.getBeanInfo(typeDesc.getTypeClass());
        Map<String, FieldDesc> fieldMap = typeDesc.getFieldMap();

        Map<String, CustomFieldDesc> customFieldMap = new HashMap<>();
        for (Schema.Field field : schema.getFields()) {
            String nsFieldName = NetSuiteDatasetRuntimeImpl.getNsFieldName(field);
            FieldDesc fieldDesc = fieldMap.get(nsFieldName);

            if (fieldDesc == null) {
                continue;
            }

            if (fieldDesc instanceof CustomFieldDesc) {
                customFieldMap.put(nsFieldName, (CustomFieldDesc) fieldDesc);
            } else {
                Object value = getSimpleProperty(nsObject, fieldDesc.getName());
                valueMap.put(nsFieldName, value);
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

    protected void writeField(Object nsObject, FieldDesc fieldDesc, Map<String, Object> customFieldMap,
            boolean replace, Collection<String> nullFieldNames, Object value) {
        if (fieldDesc instanceof CustomFieldDesc) {
            writeCustomField(nsObject, fieldDesc.asCustom(), customFieldMap, replace, nullFieldNames, value);
        } else {
            writeSimpleField(nsObject, fieldDesc.asSimple(), replace, nullFieldNames, value);
        }
    }

    protected void writeCustomField(Object nsObject, CustomFieldDesc fieldDesc, Map<String, Object> customFieldMap,
            boolean replace, Collection<String> nullFieldNames, Object value) {

        NsRef ref = fieldDesc.getRef();
        CustomFieldRefType customFieldRefType = fieldDesc.getCustomFieldType();

        Object customFieldListWrapper = getSimpleProperty(nsObject, "customFieldList");
        if (customFieldListWrapper == null) {
            customFieldListWrapper = clientService.getBasicMetaData().createInstance("CustomFieldList");
            setSimpleProperty(nsObject, "customFieldList", customFieldListWrapper);
        }
        List<Object> customFieldList = (List<Object>) getSimpleProperty(customFieldListWrapper, "customField");

        Object customField = customFieldMap.get(ref.getScriptId());
        ValueConverter valueConverter = getValueConverter(fieldDesc);

        Object targetValue = valueConverter.convertOutput(value);

        if (targetValue == null) {
            if (replace && customField != null && customFieldList != null) {
                customFieldList.remove(customField);
                nullFieldNames.add(fieldDesc.getName());
            }
        } else {
            if (customField == null) {
                customField = clientService.getBasicMetaData().createInstance(customFieldRefType.getTypeName());

                setSimpleProperty(customField, "scriptId", ref.getScriptId());
                setSimpleProperty(customField, "internalId", ref.getInternalId());

                customFieldList.add(customField);
                customFieldMap.put(ref.getScriptId(), customField);
            }

            setSimpleProperty(customField, "value", targetValue);
        }
    }

    protected void writeSimpleField(Object nsObject, SimpleFieldDesc fieldDesc,
            boolean replace, Collection<String> nullFieldNames, Object value) {

        ValueConverter valueConverter = getValueConverter(fieldDesc);

        Object targetValue = valueConverter.convertOutput(value);

        if (targetValue == null) {
            if (replace) {
                setSimpleProperty(nsObject, fieldDesc.getPropertyName(), null);
                nullFieldNames.add(fieldDesc.getName());
            }
        } else {
            setSimpleProperty(nsObject, fieldDesc.getPropertyName(), targetValue);
        }
    }

    protected Class<?> getCustomFieldValueConverterTargetClass(CustomFieldRefType customFieldRefType) {
        Class<?> valueClass;
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
        default:
            valueClass = null;
            break;
        }
        return valueClass;
    }

    public ValueConverter<?, ?> getValueConverter(FieldDesc fieldDesc) {
        Class<?> valueClass = null;
        if (fieldDesc instanceof CustomFieldDesc) {
            CustomFieldDesc customFieldDesc = (CustomFieldDesc) fieldDesc;
            CustomFieldRefType customFieldRefType = customFieldDesc.getCustomFieldType();
            valueClass = getCustomFieldValueConverterTargetClass(customFieldRefType);
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

    public ValueConverter<?, ?> getValueConverter(Class<?> valueClass) {
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
        } else if (!valueClass.isPrimitive()) {
            return new JsonValueConverter<>(objectMapper, valueClass);
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

    public static class JsonValueConverter<T> implements ValueConverter<T, String> {
        protected Class<T> clazz;
        protected ObjectReader objectReader;
        protected ObjectWriter objectWriter;

        public JsonValueConverter(ObjectMapper objectMapper, Class<T> clazz) {
            this.clazz = clazz;

            objectWriter = objectMapper.writer().forType(clazz);
            objectReader = objectMapper.reader().forType(clazz);
        }

        @Override
        public String convertInput(T value) {
            if (value == null) {
                return null;
            }
            try {
                return objectWriter.writeValueAsString(value);
            } catch (IOException e) {
                throw new NetSuiteException(new NetSuiteErrorCode("JSON_PROCESSING"), e,
                        ExceptionContext.build().put(ExceptionContext.KEY_MESSAGE, e.getMessage()));
            }
        }

        @Override
        public T convertOutput(String value) {
            if (value == null) {
                return null;
            }
            try {
                return objectReader.readValue(value);
            } catch (IOException e) {
                throw new NetSuiteException(new NetSuiteErrorCode("JSON_PROCESSING"), e,
                        ExceptionContext.build().put(ExceptionContext.KEY_MESSAGE, e.getMessage()));
            }
        }
    }

    protected static class NsTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {
        protected BasicMetaData basicMetaData;

        protected NsTypeResolverBuilder(BasicMetaData basicMetaData) {
            super(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

            this.basicMetaData = basicMetaData;

            init(JsonTypeInfo.Id.NAME, null);
            inclusion(JsonTypeInfo.As.PROPERTY);
            typeProperty(JSON_NS_TYPE_PROPERTY_NAME);
        }

        @Override
        public boolean useForType(JavaType t) {
            if (t.isCollectionLikeType()) {
                return false;
            }
            if (t.getRawClass() == XMLGregorianCalendar.class) {
                return false;
            }
            return super.useForType(t);
        }

        @Override
        protected TypeIdResolver idResolver(MapperConfig<?> config, JavaType baseType,
                Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {

            if (_idType == null) {
                throw new IllegalStateException("Can not build, 'init()' not yet called");
            }

            return new NsTypeIdResolver(baseType, config.getTypeFactory(), basicMetaData);
        }
    }

    protected static class NsTypeIdResolver extends TypeIdResolverBase {
        protected BasicMetaData basicMetaData;

        protected NsTypeIdResolver(JavaType baseType, TypeFactory typeFactory, BasicMetaData basicMetaData) {
            super(baseType, typeFactory);

            this.basicMetaData = basicMetaData;
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            Class<?> clazz = basicMetaData.getTypeClass(id);
            if (clazz == null) {
                return null;
            }
            JavaType javaType = SimpleType.construct(clazz);
            return javaType;
        }

        @Override
        public String idFromValue(Object value) {
            return value.getClass().getSimpleName();
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return suggestedType.getSimpleName();
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.NAME;
        }
    }
}
