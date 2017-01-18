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

import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroRegistry;

/**
 * In certain cases, a {@link Sink} is not the "end" of a Pipeline, but can provide records to indicate more information
 * about which writes succeeded or failed.
 * <p>
 * This looks like a transformation, but only applies to {@link Sink} runtimes. Immediately after each
 * {@link Writer#write(Object)} operation, the methods added by this interface can be called to get the list of
 * successful and rejected records. The list is only valid immediately after the write operation and will be rewritten
 * with the next "batch" on subsequent writes.
 * <p>
 * In addition, the {@link #getRejectedWrites()} is called immediately after the {@link Writer#close()} method to handle
 * any writes that might have been rejected but are not known until close is called.
 * <p>
 * The output records that an implementing component provides follow the same rules as other outputs in the component
 * framework: it is either an {@link IndexedRecord}, or provides a mechanism to convert to an {@link IndexedRecord} via
 * the {@link AvroRegistry}.
 *
 * @param MainT   the type of the output records that indicate success. This can be the same as the incoming record or can
 *                be enriched with information from the write (often the unique identifier assigned to the record on write, if any).
 * @param RejectT the type of the output records that indicate failure. This can be the same as the incoming record or
 *                can be enriched with information why the record was rejected.
 */
public interface WriterWithFeedback<WriteT, MainT, RejectT> extends Writer<WriteT> {

    /**
     * @return The list of records immediately after a write operation that were successfully written to the {@link Sink}.
     */
    Iterable<MainT> getSuccessfulWrites();

    /**
     * @return The list of records immediately after a write or close operation that were not successfully written to the {@link Sink}.
     */
    Iterable<RejectT> getRejectedWrites();

}
