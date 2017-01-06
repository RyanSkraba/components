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
package org.talend.components.simplefileio.runtime.coders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avro.mapred.AvroKey;
import org.apache.beam.sdk.coders.AtomicCoder;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;

/**
 * Coder for {@link AvroKey}.
 *
 * Normally, the work can always be shuffled to a {@link LazyAvroCoder}, since the datum in the key must be Avro
 * compatible.
 * 
 * @param <DatumT> The type of the datum stored in the key.
 */
public class LazyAvroKeyWrapper<DatumT> extends AtomicCoder<AvroKey<DatumT>> {

    private static final long serialVersionUID = 0L;

    private final LazyAvroCoder<DatumT> datumCoder;

    private LazyAvroKeyWrapper(LazyAvroCoder<DatumT> coder) {
        datumCoder = coder;
    }

    public static <T> LazyAvroKeyWrapper<T> of() {
        return new LazyAvroKeyWrapper(LazyAvroCoder.of());
    }

    public static <T> LazyAvroKeyWrapper of(LazyAvroCoder<T> coder) {
        return new LazyAvroKeyWrapper(coder);
    }

    public LazyAvroCoder<DatumT> getDatumCoder() {
        return datumCoder;
    }

    @Override
    public void encode(AvroKey<DatumT> value, OutputStream outStream, Context context) throws IOException {
        datumCoder.encode(value.datum(), outStream, context);
    }

    @Override
    public AvroKey<DatumT> decode(InputStream inStream, Context context) throws IOException {
        AvroKey<DatumT> key = new AvroKey<>();
        key.datum(datumCoder.decode(inStream, context));
        return key;
    }

}
