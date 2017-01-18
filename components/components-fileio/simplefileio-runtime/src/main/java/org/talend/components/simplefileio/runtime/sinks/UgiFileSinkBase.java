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

import java.security.PrivilegedExceptionAction;

import org.apache.beam.sdk.io.hdfs.ConfigurableHDFSFileSink;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Subclass of ConfigurableHDFSFileSink that saves {@link org.apache.hadoop.security.UserGroupInformation}.
 */
public class UgiFileSinkBase<K, V> extends ConfigurableHDFSFileSink<K, V> {

    private final UgiDoAs doAs;

    public UgiFileSinkBase(UgiDoAs doAs, String path, Class<? extends FileOutputFormat<K, V>> formatClass) {
        super(path, formatClass);
        this.doAs = doAs;
    }

    public UgiFileSinkBase(UgiDoAs doAs, String path, Class<? extends FileOutputFormat<K, V>> formatClass, Configuration conf) {
        super(path, formatClass, conf);
        this.doAs = doAs;
    }

    /**
     * Helper method for overriding the {@link ConfigureWithSampleHDFSWriter#configure(Job)} that is automatically
     * created within this sink.
     * 
     * @param job The Hadoop job containing the configuration of the format.
     * @param sample A sample of the incoming data.
     */
    protected void configure(Job job, KV<K, V> sample) {
    }

    @Override
    public WriteOperation<KV<K, V>, ?> createWriteOperation(PipelineOptions options) {
        return new UgiWriteOperation<>(this, path);
    }

    protected Writer<KV<K, V>, String> createWriter(UgiWriteOperation<K, V> writeOperation, PipelineOptions options) {
        return new UgiWriteOperation.UgiWriter<>(writeOperation, path);
    }

    public static class UgiWriteOperation<K, V> extends HDFSWriteOperation<K, V> {

        protected final UgiFileSinkBase<K, V> sink;

        public UgiWriteOperation(UgiFileSinkBase<K, V> sink, String path) {
            super(sink, path, sink.formatClass);
            this.sink = sink;
        }

        @Override
        public Writer<KV<K, V>, String> createWriter(PipelineOptions options) throws Exception {
            return sink.createWriter(this, options);
        }

        public static class UgiWriter<K, V> extends ConfigureWithSampleHDFSWriter<K, V> {

            private final UgiWriteOperation<K, V> writeOperation;

            public UgiWriter(UgiWriteOperation<K, V> writeOperation, String path) {
                super(writeOperation, path, writeOperation.formatClass);
                this.writeOperation = writeOperation;
            }

            @Override
            public void open(final String uId) throws Exception {
                this.writeOperation.sink.doAs.doAs(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws Exception {
                        ugiDoAsOpen(uId);
                        return null;
                    }
                });
            }

            protected void ugiDoAsOpen(final String uId) throws Exception {
                super.open(uId);
            }

            @Override
            public String close() throws Exception {
                return this.writeOperation.sink.doAs.doAs(new PrivilegedExceptionAction<String>() {

                    @Override
                    public String run() throws Exception {
                        return ugiDoAsClose();
                    }
                });
            }

            protected String ugiDoAsClose() throws Exception {
                return super.close();
            }

            protected void configure(Job job) {
                writeOperation.sink.configure(job, getSample());
            }
        }
    }
}
