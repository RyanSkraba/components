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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;

/**
 * Creates an {@link IndexedRecordConverter} that knows how to interpret Salesforce {@link SObject} objects.
 */
public class SObjectAdapterFactory implements IndexedRecordConverter<SObject, IndexedRecord> {

    private Schema schema;

    private String names[];

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

        public SObjectIndexedRecord(SObject value) {
            rootType = value.getType();
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
            // Lazy initialization of the cached converter objects.
            if (names == null) {
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = SalesforceAvroRegistry.get().getConverterFromString(f);
                }
            }
            Object value = valueMap.get(names[i]);
            if (value == null) {
                value = valueMap.get(rootType + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER) + names[i]);
            }
            return fieldConverter[i].convertToAvro(value);
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
                                tempPrefixTypeName = prefixTypeName + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER)
                                        + xo.getName().getLocalPart();
                            } else if (typeCount != 0 && null != typeName) {
                                // Initialize type prefix name only for child relation object.
                                tempPrefixTypeName = tempPrefixName + schema.getProp(SalesforceSchemaConstants.COLUMNNAME_DELIMTER)
                                        + typeName;
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
                valueMap.put(columnName, value);
            } else {
                if (!columnName.equals(xo.getName().getLocalPart())) {
                    valueMap.put(columnName,
                            valueMap.get(columnName) + schema.getProp(SalesforceSchemaConstants.VALUE_DELIMITER) + value);
                }
            }
        }
    }

}
