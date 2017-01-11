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
