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
package org.talend.components.simplefileio.runtime.sources;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.io.hdfs.RelaxedHDFSFileSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Extend the Beam {@link org.apache.beam.sdk.io.hdfs.HDFSFileSource} for extra functionality.
 *
 * <ul>
 * <li>To limit the number createSourceForSplit lines fetched for sampling.</li>
 * <li>To override the defaultOutputCoder if necessary..</li>
 * </ul>
 * 
 * @param <K> The key type provided by this source.
 * @param <V> The value type provided by this source.
 * @param <SourceT> The concrete implementation class for the source.
 */
public abstract class UgiFileSourceBase<K, V, SourceT extends UgiFileSourceBase<K, V, SourceT>> extends
        RelaxedHDFSFileSource<K, V> {

    protected final UgiDoAs doAs;

    /** Additional information to configure the InputFormat */
    protected final ExtraHadoopConfiguration extraConfig;

    protected UgiFileSourceBase(UgiDoAs doAs, String filepattern, Class<? extends FileInputFormat<?, ?>> formatClass,
            Class<K> keyClass, Class<V> valueClass, SerializableSplit serializableSplit) {
        this(doAs, filepattern, formatClass, keyClass, valueClass, new ExtraHadoopConfiguration(), serializableSplit);
    }

    protected UgiFileSourceBase(UgiDoAs doAs, String filepattern, Class<? extends FileInputFormat<?, ?>> formatClass,
            Class<K> keyClass, Class<V> valueClass, ExtraHadoopConfiguration extraConfig, SerializableSplit serializableSplit) {
        super(filepattern, formatClass, keyClass, valueClass, serializableSplit);
        this.doAs = doAs;
        this.extraConfig = extraConfig;
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

    /* Override with UGI if available. */
    public final long getEstimatedSizeBytes(final PipelineOptions options) {
        try {
            return doAs.doAs(new PrivilegedExceptionAction<Long>() {

                @Override
                public Long run() {
                    return doAsGetEstimatedSizeBytes(options);
                }
            });
        } catch (Exception ie) {
            throw new Pipeline.PipelineExecutionException(ie);
        }
    }

    /* Override with UGI if available. */
    @Override
    public final List<? extends BoundedSource<KV<K, V>>> splitIntoBundles(final long desiredBundleSizeBytes,
            final PipelineOptions options) throws Exception {
            return doAs.doAs(new PrivilegedExceptionAction<List<? extends BoundedSource<KV<K, V>>>>() {

                @Override
                public List<? extends BoundedSource<KV<K, V>>> run() throws Exception {
                    return doAsSplitIntoBundles(desiredBundleSizeBytes, options);
                }
            });
    }

    private long doAsGetEstimatedSizeBytes(final PipelineOptions options) {
        return super.getEstimatedSizeBytes(options);
    }

    protected List<? extends BoundedSource<KV<K, V>>> doAsSplitIntoBundles(long desiredBundleSizeBytes, PipelineOptions options)
            throws Exception {
        return super.splitIntoBundles(desiredBundleSizeBytes, options);
    }

    protected static class UgiFileReader<K, V> extends RelaxedHDFSFileReader<K, V> {

        private final UgiFileSourceBase<K, V, ?> source;

        public UgiFileReader(UgiFileSourceBase<K, V, ?> source) throws IOException {
            super(source, source.filepattern, source.formatClass, source.serializableSplit == null ? null
                    : source.serializableSplit.getSplit());
            this.source = source;
            source.extraConfig.addTo(job.getConfiguration());
        }

        @Override
        public final boolean advance() throws IOException {
            try {
                return source.doAs.doAs(new PrivilegedExceptionAction<Boolean>() {

                    @Override
                    public Boolean run() throws Exception {
                        return doAsAdvance();
                    }
                });
            } catch (Exception e) {
                if (e instanceof IOException)
                    throw (IOException) e;
                else
                    throw new Pipeline.PipelineExecutionException(e);
            }
        }

        public boolean doAsAdvance() throws IOException {
            return super.advance();
        }
    }
}
