// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;

/**
 * Contains multiple partitions of {@link IndexedRecord} reusable for testing.
 */
public class RecordSet {

    /** The identifier of the record set. */
    private final String uid;

    private final Schema schema;

    private final List<List<IndexedRecord>> partitions;

    private List<IndexedRecord> allData = null;

    public RecordSet(String name, List<IndexedRecord>... data) {
        this.uid = name;
        Schema s = null;
        if (data.length > 0 && data[0].size() > 0)
            s = data[0].get(0).getSchema();
        schema = s;
        this.partitions = Arrays.asList(data);
    }

    public String getUid() {
        return uid;
    }

    public Schema getSchema() {
        return schema;
    }

    public List<List<IndexedRecord>> getPartitions() {
        return partitions;
    }

    public List<IndexedRecord> getAllData() {
        if (allData == null) {
            allData = new ArrayList<>();
            for (List<IndexedRecord> r : this.partitions) {
                allData.addAll(r);
            }
        }
        return allData;
    }

}
