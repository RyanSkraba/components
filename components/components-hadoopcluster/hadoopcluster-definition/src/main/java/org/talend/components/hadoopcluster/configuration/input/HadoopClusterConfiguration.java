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

package org.talend.components.hadoopcluster.configuration.input;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;

/**
 * This class represent hadoop configuration for hadoop cluster manager API, which include
 * cluster name, service name, configuration file name and the content in xml format.
 */
public class HadoopClusterConfiguration implements IndexedRecord {

    public static final Schema schema;

    static {
        schema = SchemaBuilder.record("hadoopconfiguration").fields() //
                .name("clusterName").type().stringType().noDefault() //
                .name("serviceName").type().stringType().noDefault() //
                .name("confFileName").type().stringType().noDefault() //
                .name("confFileContent").type().stringType().noDefault() //
                .endRecord();
    }

    private String[] contents = new String[4];

    public HadoopClusterConfiguration(String clusterName, String serviceName, String confFileName, String confFileContent) {
        put(0, clusterName);
        put(1, serviceName);
        put(2, confFileName);
        put(3, confFileContent);
    }

    @Override
    public void put(int i, Object v) {
        String content = v == null ? null : String.valueOf(v);
        contents[i] = content;
    }

    @Override
    public Object get(int i) {
        return contents[i];
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return contents[0] + contents[1] + contents[2] + contents[3];
    }
}
