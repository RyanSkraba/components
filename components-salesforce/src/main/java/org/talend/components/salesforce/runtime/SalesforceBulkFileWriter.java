// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.BulkFileProperties;
import org.talend.components.common.runtime.BulkFileWriter;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;

/**
 * Prepare Data Files for bulk execution
 */
final class SalesforceBulkFileWriter extends BulkFileWriter {

    public SalesforceBulkFileWriter(WriteOperation<WriterResult> writeOperation, BulkFileProperties bulkProperties,
            RuntimeContainer adaptor) {
        super(writeOperation, bulkProperties, adaptor);
    }

    @Override
    public String[] getHeaders(Schema schema) {
        TSalesforceOutputBulkProperties salesforceBulkProperties = (TSalesforceOutputBulkProperties) bulkProperties;

        List<String> headers = new ArrayList<String>();
        StringBuilder sbuilder = new StringBuilder();
        for (Schema.Field f : schema.getFields()) {
            String header = f.name();

            String ref_module_name = f.getProp(SalesforceSchemaConstants.REF_MODULE_NAME);
            String ref_field_name = f.getProp(SalesforceSchemaConstants.REF_FIELD_NAME);
            if (ref_module_name != null) {
                header = sbuilder.append(ref_module_name).append(":").append(ref_field_name).append(".").append(f.name())
                        .toString();
                sbuilder.setLength(0);
            } else {
                Object value = salesforceBulkProperties.upsertRelationTable.columnName.getValue();
                if (value != null && value instanceof List) {
                    int index = getIndex((List<String>) value, header);
                    if (index > -1) {
                        List<String> polymorphics = salesforceBulkProperties.upsertRelationTable.polymorphic.getValue();
                        List<String> lookupFieldModuleNames = salesforceBulkProperties.upsertRelationTable.lookupFieldModuleName
                                .getValue();
                        List<String> lookupFieldNames = salesforceBulkProperties.upsertRelationTable.lookupFieldName.getValue();
                        List<String> externalIdFromLookupFields = salesforceBulkProperties.upsertRelationTable.lookupFieldExternalIdName
                                .getValue();

                        if ("true".equals(polymorphics.get(index))) {
                            sbuilder.append(lookupFieldModuleNames.get(index)).append(":");
                        }
                        sbuilder.append(lookupFieldNames.get(index)).append(".").append(externalIdFromLookupFields.get(index));
                        header = sbuilder.toString();
                        sbuilder.setLength(0);
                    }
                }
            }

            headers.add(header);
        }
        return headers.toArray(new String[headers.size()]);
    }

    private int getIndex(List<String> columnNames, String columnName) {
        if (columnNames == null) {
            return -1;
        }
        return columnNames.indexOf(columnName);
    }

    @Override
    public List<String> getValues(Object datum) {
        IndexedRecord input = getFactory(datum).convertToAvro((IndexedRecord) datum);
        List<String> values = new ArrayList<String>();
        for (Schema.Field f : input.getSchema().getFields()) {
            if (input.get(f.pos()) == null) {
                if (((TSalesforceOutputBulkProperties) bulkProperties).ignoreNull.getValue()) {
                    values.add("");
                } else {
                    values.add("#N/A");
                }
            } else {
                values.add(String.valueOf(input.get(f.pos())));
            }
        }
        return values;
    }

    @Override
    public WriterResult close() throws IOException {
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), SalesforceOutputProperties.NB_LINE, dataCount);
        }
        return super.close();
    }
}