/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.talend.components.api.component.runtime;

import java.util.List;

import org.talend.components.api.container.RuntimeContainer;

/**
 * A {@link Source} that reads a finite amount of input and, because of that, supports some additional operations.
 *
 * <p>
 * The operations are:
 * <ul>
 * <li>Splitting into bundles of given size: {@link #splitIntoBundles};
 * <li>Size estimation: {@link #getEstimatedSizeBytes};
 * <li>Telling whether or not this source produces key/value pairs in sorted order: {@link #producesSortedKeys};
 * <li>The reader ({@link BoundedReader}) supports progress estimation ({@link BoundedReader#getFractionConsumed}) and dynamic
 * splitting ({@link BoundedReader#splitAtFraction}).
 * </ul>
 *
 */
public interface BoundedSource extends Source {

    /**
     * Splits the source into bundles of approximately {@code desiredBundleSizeBytes}.
     */
    List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception;

    /**
     * An estimate of the total size (in bytes) of the data that would be read from this source. This estimate is in
     * terms of external storage size, before any decompression or other processing done by the reader.
     */
    long getEstimatedSizeBytes(RuntimeContainer adaptor);

    /**
     * Whether this source is known to produce key/value pairs sorted by lexicographic order on the bytes of the encoded
     * key.
     */
    boolean producesSortedKeys(RuntimeContainer adaptor);

    /**
     * Returns a new {@link BoundedReader} that reads from this source.
     */
    BoundedReader createReader(RuntimeContainer adaptor);

}
