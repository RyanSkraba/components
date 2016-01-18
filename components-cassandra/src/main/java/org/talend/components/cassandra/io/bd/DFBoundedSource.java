package org.talend.components.cassandra.io.bd;

import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.io.BoundedSource;
import com.google.cloud.dataflow.sdk.options.DataflowPipelineWorkerPoolOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import org.talend.components.api.component.runtime.input.SingleSplit;
import org.talend.components.api.component.runtime.input.Source;
import org.talend.components.api.component.runtime.input.Split;
import org.talend.components.api.properties.ComponentProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by bchen on 16-1-17.
 */
public class DFBoundedSource extends BoundedSource {
    Source source;
    Split split;

    public DFBoundedSource(Class<? extends Source> sourceClazz, ComponentProperties props) {
        try {
            this.source = sourceClazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.source.init(props);
    }

    public DFBoundedSource(Split split) {
        this.split = split;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, PipelineOptions options) throws Exception {
        List<DFBoundedSource> sourceList = new ArrayList<>();
        if (source.supportSplit()) {
            DataflowPipelineWorkerPoolOptions poolOptions =
                    options.as(DataflowPipelineWorkerPoolOptions.class);
            Split[] split = source.getSplit(poolOptions.getNumWorkers());
            for (Split s : split) {
                sourceList.add(new DFBoundedSource(s));
            }
        } else {
            sourceList.add(new DFBoundedSource(new SingleSplit()));
        }
        return sourceList;
    }

    @Override
    public long getEstimatedSizeBytes(PipelineOptions options) throws Exception {
        //TODO source.getCount?
        return 0;
    }

    @Override
    public boolean producesSortedKeys(PipelineOptions options) throws Exception {
        return false;
    }

    @Override
    public BoundedReader createReader(PipelineOptions options) throws IOException {
        return new DFBoundedReader(source.getRecordReader(split));
    }

    @Override
    public void validate() {

    }

    @Override
    public Coder getDefaultOutputCoder() {
        return null;
    }

    public class DFBoundedReader extends BoundedReader {
        org.talend.components.api.component.runtime.input.Reader reader;

        public DFBoundedReader(org.talend.components.api.component.runtime.input.Reader reader) {
            this.reader = reader;
        }

        @Override
        public boolean start() throws IOException {
            return reader.start();
        }

        @Override
        public boolean advance() throws IOException {
            return reader.advance();
        }

        @Override
        public Object getCurrent() throws NoSuchElementException {
            return reader.getCurrent();
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        @Override
        public BoundedSource getCurrentSource() {
            return null;
        }
    }
}
