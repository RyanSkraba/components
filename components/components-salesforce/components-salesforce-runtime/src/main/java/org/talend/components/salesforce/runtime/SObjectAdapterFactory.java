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
package org.talend.components.salesforce.runtime;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.salesforce.runtime.SalesforceAvroRegistry.StringToDateConverter;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.CalendarCodec;
import com.sforce.ws.bind.XmlObject;
import com.sforce.ws.util.Base64;

/**
 * Creates an {@link IndexedRecordConverter} that knows how to interpret Salesforce {@link SObject} objects.
 */
public class SObjectAdapterFactory implements IndexedRecordConverter<SObject, IndexedRecord> {

    private Schema schema;

    private String names[];

    private Map<String, AvroConverter> name2converter;

    /**
     * The cached AvroConverter objects for the fields of this record.
     */
    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Class<SObject> getDatumClass() {
        return SObject.class;
    }

    @Override
    public SObject convertToDatum(IndexedRecord value) {
        throw new UnmodifiableAdapterException();
    }

    @Override
    public IndexedRecord convertToAvro(SObject value) {
        return new SObjectIndexedRecord(value);
    }

    private class SObjectIndexedRecord implements IndexedRecord {

        private Map<String, Object> valueMap;

        private String rootType;

        private boolean isAggregateResult;

        public SObjectIndexedRecord(SObject value) {
            rootType = value.getType();
            isAggregateResult = "AggregateResult".equals(rootType);
            init();

            Iterator<XmlObject> fields = value.getChildren();
            while (fields.hasNext()) {
                XmlObject field = fields.next();
                if (valueMap != null && (valueMap.containsKey(field.getName().getLocalPart()) || valueMap.containsKey(rootType
                        + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER) + field.getName().getLocalPart()))) {
                    continue;
                } else {
                    processXmlObject(field, rootType, null);
                }
            }
        }

        @Override
        public Schema getSchema() {
            return SObjectAdapterFactory.this.getSchema();
        }

        @Override
        public void put(int i, Object v) {
            throw new UnmodifiableAdapterException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object get(int i) {
            Object value = valueMap.get(names[i]);
            if (value == null) {
                value = valueMap.get(rootType + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER) + names[i]);
            }
            return fieldConverter[i].convertToAvro(value);
        }

        private void init() {
            if (names == null) {
                List<Schema.Field> fields = getSchema().getFields();
                names = new String[fields.size()];
                fieldConverter = new AvroConverter[names.length];
                name2converter = new HashMap<String, AvroConverter>();
                for (int j = 0; j < names.length; j++) {
                    Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = SalesforceAvroRegistry.get().getConverterFromString(f);
                    name2converter.put(f.name(), fieldConverter[j]);
                    name2converter.put(rootType + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER) + f.name(), fieldConverter[j]);
                }
            }
        }

        /**
         * Parse XML object and store found values into value map.
         * During iterations <code>prefixName</code> concatenates with current local part of input object.
         * On the last iteration prefixName will become as a column name.
         * <code>Id</code> and <code>type</code> elements are skipped, except type of child relation.
         *
         *
         * @param xo - XML object to be processed.
         * @param prefixName - name of all parent branches, appended with underscore.
         * @param prefixTypeName - name of child relation.
         */
        protected void processXmlObject(XmlObject xo, String prefixName, String prefixTypeName) {
            if (valueMap == null) {
                valueMap = new HashMap<>();
            }
            Iterator<XmlObject> xos = xo.getChildren();
            if (xos.hasNext()) {
                // delete the fixed id and type elements when find firstly
                int typeCount = 0;
                int idCount = 0;
                String typeName = null;
                while (xos.hasNext()) {
                    XmlObject objectValue = xos.next();
                    if (objectValue != null) {
                        XmlObject xmlObject = objectValue;

                        if ("type".equals(xmlObject.getName().getLocalPart()) && typeCount == 0) {
                            typeCount++;
                            typeName = xmlObject.getValue().toString();
                            continue;
                        }
                        if ("Id".equals(xmlObject.getName().getLocalPart()) && idCount == 0) {
                            idCount++;
                            continue;
                        }
                        if (null != prefixName) {
                            String tempPrefixName = prefixName + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER)
                                    + xo.getName().getLocalPart();
                            String tempPrefixTypeName = null;
                            if (null != prefixTypeName) {
                                tempPrefixTypeName = prefixTypeName
                                        + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER)
                                        + xo.getName().getLocalPart();
                            } else if (typeCount != 0 && null != typeName) {
                                // Initialize type prefix name only for child relation object.
                                tempPrefixTypeName = tempPrefixName
                                        + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER) + typeName;
                            }
                            processXmlObject(xmlObject, tempPrefixName, tempPrefixTypeName);
                        } else {
                            processXmlObject(xmlObject, xo.getName().getLocalPart(), prefixTypeName);
                        }
                    }
                }
            } else {
                placeValueInFieldMap(prefixName, xo);
                // Extended columns for parent-to-child relation with the same values.
                if (null != prefixTypeName) {
                    placeValueInFieldMap(prefixTypeName, xo);
                }
            }
        }

        final private CalendarCodec calendarCodec = new CalendarCodec();

        /**
         * Puts parsed values into value map by column names or complex column names.<br/>
         * For <b>Parent-to-Child</b> relation stores duplicates to grant a possibility<br/>
         * to get values by such column names in child table:
         * <code>Contact.Name, Contact.Account.Name</code>
         *
         * @param prefixName - name to be appended to column name.
         * @param xo - XML object that contains column value.
         */
        private void placeValueInFieldMap(String prefixName, XmlObject xo) {
            Object value = xo.getValue();
            if (value == null || "".equals(value)) {
                return;
            }

            String columnName = null;
            if (prefixName != null && prefixName.length() > 0) {
                columnName = prefixName + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER)
                        + xo.getName().getLocalPart();
            } else {
                columnName = xo.getName().getLocalPart();
            }

            if (valueMap.get(columnName) == null) {
                valueMap.put(columnName, formatIfNecessary(value, columnName));
            } else {
                if (!columnName.equals(xo.getName().getLocalPart())) {
                    valueMap.put(columnName, valueMap.get(columnName) + schema.getProp(SalesforceSchemaConstants.VALUE_DELIMITER)
                            + formatIfNecessary(value, columnName));
                }
            }
        }

        private String formatIfNecessary(Object value, String columnName) {
            final String text;
            if (isAggregateResult) {
                if (value instanceof String) {
                    text = value.toString();
                } else if (value instanceof Integer) {
                    text = "" + value;
                } else if (value instanceof BigDecimal) {
                    text = ((BigDecimal) value).toPlainString();
                } else if (value instanceof Long) {
                    text = "" + value;
                } else if (value instanceof Date) {
                    AvroConverter converter = name2converter.get(columnName);
                    if ((converter == null) || !(converter instanceof StringToDateConverter)) {
                        text = calendarCodec.getValueAsString(value);
                    } else {
                        StringToDateConverter stdc = (StringToDateConverter) converter;
                        text = stdc.getFormat().format((Date) value);
                    }
                } else if (value instanceof Calendar) {
                    AvroConverter converter = name2converter.get(columnName);
                    if ((converter == null) || !(converter instanceof StringToDateConverter)) {
                        text = calendarCodec.getValueAsString(value);
                    } else {
                        StringToDateConverter stdc = (StringToDateConverter) converter;
                        text = stdc.getFormat().format(((Calendar) value).getTime());
                    }
                } else if (value instanceof Boolean) {
                    text = "" + value;
                } else if (value instanceof Double) {
                    text = ((Double) value).toString();
                } else if (value instanceof Float) {
                    text = "" + value;
                } else if (value instanceof byte[]) {
                    text = new String(Base64.encode((byte[]) value));
                } else {
                    text = value.toString();
                }
            } else {
                text = value.toString();
            }
            return text;
        }
    }

}
