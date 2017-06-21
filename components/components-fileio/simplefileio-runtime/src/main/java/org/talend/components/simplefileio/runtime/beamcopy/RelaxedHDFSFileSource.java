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
package org.talend.components.simplefileio.runtime.beamcopy;

import java.io.IOException;

import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * This class exists only to relax the package-private access to the subclass for extension.
 */
public abstract class RelaxedHDFSFileSource<K, V> extends ConfigurableHDFSFileSource<K, V> {

    protected RelaxedHDFSFileSource(String filepattern, Class<? extends FileInputFormat<?, ?>> formatClass, Class<K> keyClass,
            Class<V> valueClass, SerializableSplit serializableSplit) {
        super(filepattern, formatClass, keyClass, valueClass, serializableSplit);
    }

    protected static class RelaxedHDFSFileReader<K, V> extends HDFSFileReader<K, V> {

        protected RelaxedHDFSFileReader(BoundedSource<KV<K, V>> source, String filepattern,
                Class<? extends FileInputFormat<?, ?>> formatClass, InputSplit split) throws IOException {
            super(source, filepattern, formatClass, split);
        }
    }

}