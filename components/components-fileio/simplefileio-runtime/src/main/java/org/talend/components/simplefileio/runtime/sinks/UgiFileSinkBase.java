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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.beamcopy.ConfigurableHDFSFileSink;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Subclass of ConfigurableHDFSFileSink that saves {@link org.apache.hadoop.security.UserGroupInformation}.
 *
 * If the path is the local filesystem, the UGI is still used, but the job is configured to ignore any existing default
 * filesystem information.
 */
public class UgiFileSinkBase<K, V> extends ConfigurableHDFSFileSink<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(UgiFileSinkBase.class);

    private final UgiDoAs doAs;

    private final boolean overwrite;

    /** Additional information to configure the OutputFormat */
    private final ExtraHadoopConfiguration extraConfig;

    public UgiFileSinkBase(UgiDoAs doAs, String path, boolean overwrite, boolean mergeOutput,
            Class<? extends FileOutputFormat<K, V>> formatClass) {
        this(doAs, path, overwrite, mergeOutput, formatClass, new ExtraHadoopConfiguration());
    }

    public UgiFileSinkBase(UgiDoAs doAs, String path, boolean overwrite, boolean mergeOutput,
            Class<? extends FileOutputFormat<K, V>> formatClass, ExtraHadoopConfiguration extraConfig) {
        super(path, mergeOutput, formatClass);
        this.doAs = doAs;
        this.overwrite = overwrite;
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
        try {
            Job job = jobInstance();
            FileSystem fs = FileSystem.get(new URI(path), job.getConfiguration());
            checkState(!fs.exists(new Path(path)) || overwrite, "Output path " + path + " already exists");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
    public WriteOperation<KV<K, V>, ?> createWriteOperation() {
        return new UgiWriteOperation<>(this, path, mergeOutput);
    }

    protected Writer<KV<K, V>, String> createWriter(UgiWriteOperation<K, V> writeOperation, PipelineOptions options) {
        return new UgiWriteOperation.UgiWriter<>(writeOperation, path);
    }

    protected boolean mergeOutput(FileSystem fs, String sourceFolder, String targetFile) {
        // implement how to merge files, different between format
        try {
            return copyMerge(fs, new Path(sourceFolder), fs, new Path(targetFile), fs.getConf());
        } catch (Exception e) {
            LOG.error("Error when merging files in {}.\n{}", sourceFolder, e.getMessage());
            return false;
        }
    }

    /** Copy all files in a directory to one output file (merge). */
    private static boolean copyMerge(FileSystem srcFS, Path srcDir, FileSystem dstFS, Path dstFile, Configuration conf)
            throws IOException {

        if (!srcFS.getFileStatus(srcDir).isDirectory())
            return false;

        // Unlike org.apache.hadoop.fs.FileUtil#copyMerge, make sure that we list the contents of the input directory
        // BEFORE creating the destination file. Otherwise we might end in a loop that generates an output file of
        // infinite size when the dstFile is in the srcDir.
        FileStatus contents[] = srcFS.listStatus(srcDir);
        OutputStream out = dstFS.create(dstFile);

        try {
            Arrays.sort(contents);
            for (int i = 0; i < contents.length; i++) {
                if (contents[i].isFile()) {
                    InputStream in = srcFS.open(contents[i].getPath());
                    try {
                        IOUtils.copyBytes(in, out, conf, false);
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            out.close();
        }
        return true;
    }

    public static class UgiWriteOperation<K, V> extends HDFSWriteOperation<K, V> {

        protected final UgiFileSinkBase<K, V> sink;

        public UgiWriteOperation(UgiFileSinkBase<K, V> sink, String path, boolean mergeOutput) {
            super(sink, path, mergeOutput, sink.formatClass);
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

        @Override
        protected boolean mergeOutput(FileSystem fs, String sourceFolder, String targetFile) {
            return this.sink.mergeOutput(fs, sourceFolder, targetFile);
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
