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
package org.talend.components.simplefileio.runtime.sinks;

import org.apache.beam.sdk.io.hdfs.ConfigurableHDFSFileSink;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * An {@link org.apache.beam.sdk.io.hdfs.ConfigurableHDFSFileSink.HDFSWriter} subclass that delays opening the
 * underlying write operation until after the first attempt to write a value.
 */
public class ConfigureWithSampleHDFSWriter<K, V> extends ConfigurableHDFSFileSink.HDFSWriter<K, V> {

    private final Class<? extends FileOutputFormat<K, V>> formatClass;

    private String uId;

    private boolean opened = false;

    private KV<K, V> sample;

    public ConfigureWithSampleHDFSWriter(ConfigurableHDFSFileSink.HDFSWriteOperation<K, V> writeOperation, String path,
            Class<? extends FileOutputFormat<K, V>> formatClass) {
        super(writeOperation, path, formatClass);
        this.formatClass = formatClass;
    }

    @Override
    public void open(String uId) throws Exception {
        this.uId = uId;
    }

    @Override
    public void write(KV<K, V> value) throws Exception {
        // Open on the first write.
        if (!opened) {
            opened = true;
            // Ensure that a sample is available during open, and clean it up after.
            sample = value;
            super.open(uId);
            sample = null;
        }
        super.write(value);
    }

    @Override
    public String close() throws Exception {
        // If data was written, then close the task normally.
        if (opened) {
            return super.close();
        }
        // If no data was written, then return null to indicate that this writer did not produce any output.
        return null;
    }

    protected KV<K, V> getSample() {
        return sample;
    }
}
