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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.repackaged.com.google.common.base.Function;
import org.apache.beam.sdk.repackaged.com.google.common.collect.ImmutableList;
import org.apache.beam.sdk.repackaged.com.google.common.collect.Lists;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Extend the Beam {@link org.apache.beam.sdk.io.hdfs.HDFSFileSource} for extra functionality.
 *
 * <ul>
 * <li>To limit the number of lines fetched for sampling.</li>
 * <li>To override the defaultOutputCoder if necessary..</li>
 * </ul>
 * 
 * @param <K> The key type provided by this source.
 * @param <V> The value type provided by this source.
 * @param <SourceT> The concrete implementation class for the source.
 */
public abstract class FileSourceBase<K, V, SourceT extends FileSourceBase<K, V, SourceT>>
        extends UgiFileSourceBase<K, V, SourceT> {

    private Coder<KV<K, V>> defaultCoder;

    private int limit = -1;

    /**
     * This static field is used to ensure limits are respected when running the FileSource in the DirectRunner and is
     * only accessed when limit is non-negative.
     * 
     * This is a workaround to the fact that collections are completely materialized when using the DirectRunner, by
     * causing the readers to abort when enough records have been read. In a normal distributed pipeline, the
     * {@link org.apache.beam.sdk.transforms.Sample#any(long)} method should be used instead.
     * 
     * There are two important points to take into account:
     * 
     * 1) There can only be one running FileSourceBase running at a time, since it is static.
     * 
     * 2) However, since the component service uses a different classloader for each getSample() request, each request
     * will have its own static scope for this variable and multple getSample requests can be simultaneously processed.
     */
    private static AtomicInteger sharedCount = new AtomicInteger(0);

    protected FileSourceBase(UgiDoAs doAs, String filepattern, Class<? extends FileInputFormat<?, ?>> formatClass,
            Class<K> keyClass, Class<V> valueClass, SerializableSplit serializableSplit) {
        super(doAs, filepattern, formatClass, keyClass, valueClass, serializableSplit);
    }

    protected FileSourceBase(UgiDoAs doAs, String filepattern, Class<? extends FileInputFormat<?, ?>> formatClass,
            Class<K> keyClass, Class<V> valueClass, ExtraHadoopConfiguration extraConfig, SerializableSplit serializableSplit) {
        super(doAs, filepattern, formatClass, keyClass, valueClass, extraConfig, serializableSplit);
    }

    /**
     * Factory method to create a source of the same type as the concrete implementation, with the same parameters but
     * for the given split.
     * 
     * @param serializableSplit the split that the source is processing.
     * @return a source configured for the split.
     */
    protected abstract SourceT createSourceForSplit(SerializableSplit serializableSplit);

    /**
     * @param serializableSplit the split that the source is processing.
     * @return a reader created for this source.
     * @throws IOException If the reader can't be created.
     */
    protected abstract BoundedSource.BoundedReader<KV<K, V>> createReaderForSplit(SerializableSplit serializableSplit)
            throws IOException;

    protected void setDefaultCoder(Coder<K> keyCoder, Coder<V> valueCoder) {
        this.defaultCoder = KvCoder.of(keyCoder, valueCoder);
    }

    @Override
    public Coder<KV<K, V>> getDefaultOutputCoder() {
        if (defaultCoder != null)
            return defaultCoder;
        return super.getDefaultOutputCoder();
    }

    public void setLimit(int limit) {
        this.limit = limit;
        // Reset the shared state whenever the limit is set to non-negative.
        if (limit >= 0)
            sharedCount.set(0);
    }

    protected int getLimit() {
        return limit;
    }

    @Override
    protected List<? extends BoundedSource<KV<K, V>>> doAsSplitIntoBundles(long desiredBundleSizeBytes, PipelineOptions options)
            throws Exception {
        // Re-implementation of the base class method to use the factory methods.
        long splitSize = limit >= 0 ? Math.max(desiredBundleSizeBytes, 10 * 1024 * 1024) : desiredBundleSizeBytes;

        if (serializableSplit == null) {
            return Lists.transform(computeSplits(splitSize), new Function<InputSplit, BoundedSource<KV<K, V>>>() {

                @Override
                public BoundedSource<KV<K, V>> apply(@Nullable InputSplit inputSplit) {
                    return createSourceForSplit(new SerializableSplit(inputSplit));
                }
            });
        } else {
            return ImmutableList.of(this);
        }
    }

    @Override
    public BoundedReader<KV<K, V>> createReader(PipelineOptions options) throws IOException {
        // Re-implementation of the base class method to use the factory methods.
        this.validate();
        if (limit < 0)
            return createReaderForSplit(serializableSplit);
        else
            return BoundedReaderWithLimit.of(createReaderForSplit(serializableSplit), getLimit(), sharedCount);
    }
}
