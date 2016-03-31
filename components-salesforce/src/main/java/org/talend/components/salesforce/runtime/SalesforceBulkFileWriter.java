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

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.BulkFileProperties;
import org.talend.components.common.runtime.BulkFileWriter;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Prepare Data Files for bulk execution
 */
final class SalesforceBulkFileWriter extends BulkFileWriter {

    public SalesforceBulkFileWriter(WriteOperation<WriterResult> writeOperation, BulkFileProperties bulkProperties, RuntimeContainer adaptor) {
        super(writeOperation, bulkProperties, adaptor);
    }

    @Override
    public String[] getHeaders(Schema schema){
        //  FIXME when "upsertRelation" table can work
       return super.getHeaders(schema);
    }

    @Override
    public List<String> getValues(Object datum){
        IndexedRecord input = getFactory(datum).convertToAvro(datum);
        List<String> values = new ArrayList<String>();
        for (Schema.Field f : input.getSchema().getFields()) {
            if(input.get(f.pos())==null){
                if(((TSalesforceOutputBulkProperties)bulkProperties).ignoreNull.getBooleanValue()){
                    values.add("");
                }else{
                    values.add("#N/A");
                }
            }else{
                values.add(String.valueOf(input.get(f.pos())));
            }
        }
        return values;
    }
}