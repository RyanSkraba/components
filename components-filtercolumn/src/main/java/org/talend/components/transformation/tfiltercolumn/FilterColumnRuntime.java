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
package org.talend.components.transformation.tfiltercolumn;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.DoubleOutputConnector;
import org.talend.components.api.runtime.TransformationRuntime;

public class FilterColumnRuntime implements TransformationRuntime<IndexedRecord, IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(FilterColumnRuntime.class);

    Schema inSchema;

    int inColumnToDelete = -1;

    Schema outSchema;

    @Override
    public void execute(IndexedRecord inputValue, DoubleOutputConnector<IndexedRecord, IndexedRecord> outputs) throws Exception {
        // This analysis should probably have been done outside...
        if (inSchema == null) {
            inSchema = inputValue.getSchema();
            List<Field> fields = inSchema.getFields();
            for (Field f : fields) {
                if (f.name().equals("invalid"))
                    inColumnToDelete = f.pos();
            }
            if (inColumnToDelete != -1) {
                outSchema = Schema.createRecord(inSchema.getName(), inSchema.getDoc(), inSchema.getNamespace(),
                        inSchema.isError());
                outSchema.getJsonProps().putAll(inSchema.getJsonProps());
                fields = new ArrayList<Field>(fields);
                fields.remove(inColumnToDelete);
                outSchema.setFields(fields);
                ;
            }
        }

        if (inColumnToDelete == -1) {
            outputs.outputMainData(inputValue);
        } else {
            outputs.outputMainData((IndexedRecord) SpecificData.get().newRecord(null, outSchema));
        }
    }

    @Override
    public void setUp(ComponentProperties context) {
    }

    @Override
    public void tearDown() {
        // do nothing on purpose
    }
}
