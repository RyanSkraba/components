// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.facet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.coders.StandardCoder;

/**
 * Method to serialize BDObject with Kryo. We may transform it to convert any type of object with Kryo
 *
 */
public class KryoCoder<T> extends StandardCoder<T> implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * Returns a {@code WritableCoder} instance for the provided element class.
     *
     * @param <T> the element type
     */
    public static <T> KryoCoder of() {
        return new KryoCoder<T>();
    }

    String test = "";

    public KryoCoder() {
    }

    @Override
    public void encode(T value, OutputStream outStream, Context context) throws IOException {
        Output output = new Output(outStream);
        Kryo kryo = new Kryo();
        kryo.writeClassAndObject(output, value);
        test = value.getClass().toString();
        output.flush();

    }

    @Override
    public T decode(InputStream inStream, Context context) throws IOException {

        Input input = new Input(inStream);
        Kryo kryo = new Kryo();
        T current = (T) kryo.readClassAndObject(input);
        return current;
    }

    @Override
    public List<Coder<?>> getCoderArguments() {
        return null;
    }

    // TODO Fix Error Message
    @Override
    public void verifyDeterministic() throws NonDeterministicException {
        throw new NonDeterministicException(this, "Error message.");
    }

}
