package org.talend.components.api.component.runtime.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * A simplified interface for input sources that can never be split across multiple shards and will only be run within
 * one worker/thread.
 *
 * @param <T> The type of row that this source will generate.
 */
public interface UnshardedInput<T> extends Iterator<T>, Serializable, Closeable {

    public void setup() throws IOException;
}