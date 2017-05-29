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

import java.io.IOException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.apache.beam.sdk.io.hdfs.ConfigurableHDFSFileSink;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Subclass of ConfigurableHDFSFileSink that saves {@link org.apache.hadoop.security.UserGroupInformation}.
 *
 * If the path is the local filesystem, the UGI is still used, but the job is configured to ignore any existing default
 * filesystem information.
 */
public class UgiFileSinkBase<K, V> extends ConfigurableHDFSFileSink<K, V> {

    private final UgiDoAs doAs;

    /** Additional information to configure the OutputFormat */
    private final ExtraHadoopConfiguration extraConfig;

    public UgiFileSinkBase(UgiDoAs doAs, String path, Class<? extends FileOutputFormat<K, V>> formatClass) {
        this(doAs, path, formatClass, new ExtraHadoopConfiguration());
    }

    public UgiFileSinkBase(UgiDoAs doAs, String path, Class<? extends FileOutputFormat<K, V>> formatClass,
            ExtraHadoopConfiguration extraConfig) {
        super(path, formatClass);
        this.doAs = doAs;
        this.extraConfig = extraConfig;
        // Ensure that the local filesystem is used if the path starts with the file:// schema.
        if (path.toLowerCase().startsWith("file:")) {
            this.extraConfig.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY,
                    CommonConfigurationKeysPublic.FS_DEFAULT_NAME_DEFAULT);
        }
    }

    @Override
    public void validate(final PipelineOptions options) {
        doAs.doAs(new PrivilegedAction<Void>() {

            @Override
            public Void run() {
                ugiDoAsValidate(options);
                return null;
            }
        });
    }

    protected void ugiDoAsValidate(final PipelineOptions options) {
        super.validate(options);
    }

    public ExtraHadoopConfiguration getExtraHadoopConfiguration() {
        return extraConfig;
    }

    @Override
    protected Job jobInstance() throws IOException {
        Job job = super.jobInstance();
        extraConfig.addTo(job.getConfiguration());
        return job;
    }

    /**
     * Helper method for overriding the {@link ConfigureWithSampleHDFSWriter#configure(Job)} that is automatically
     * created within this sink.
     * 
     * @param job The Hadoop job containing the configuration of the format.
     * @param sample A sample of the incoming data.
     */
    protected void configure(Job job, KV<K, V> sample) {
        // The extra configuration has already been added to the job.
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
        public void finalize(final Iterable<String> writerResults, final PipelineOptions options) throws Exception {
            this.sink.doAs.doAs(new PrivilegedExceptionAction<Void>() {

                @Override
                public Void run() throws Exception {
                    ugiDoAsFinalize(writerResults, options);
                    return null;
                }
            });
        }

        protected void ugiDoAsFinalize(Iterable<String> writerResults, PipelineOptions options) throws Exception {
            super.finalize(writerResults, options);
        }

        @Override
        public Writer<KV<K, V>, String> createWriter(PipelineOptions options) throws Exception {
            return sink.createWriter(this, options);
        }

        public static class UgiWriter<K, V> extends ConfigureWithSampleHDFSWriter<K, V> {

            private final UgiWriteOperation<K, V> writeOperation;

            private final String path;

            public UgiWriter(UgiWriteOperation<K, V> writeOperation, String path) {
                super(writeOperation, path, writeOperation.formatClass);
                this.writeOperation = writeOperation;
                this.path = path;
            }

            @Override
            protected void superOpen(final String uId) throws Exception {
                this.writeOperation.sink.doAs.doAs(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws Exception {
                        ugiDoAsSuperOpen(uId);
                        return null;
                    }
                });
            }

            protected void ugiDoAsSuperOpen(final String uId) throws Exception {
                super.superOpen(uId);
            }

            @Override
            public void write(final KV<K, V> value) throws Exception {
                this.writeOperation.sink.doAs.doAs(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws Exception {
                        ugiDoAsWrite(value);
                        return null;
                    }
                });
            }

            protected void ugiDoAsWrite(KV<K, V> value) throws Exception {
                super.write(value);
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
