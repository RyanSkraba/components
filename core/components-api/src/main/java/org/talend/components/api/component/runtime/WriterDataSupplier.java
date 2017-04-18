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

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.lang3.RandomStringUtils;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.java8.Supplier;

/**
 * Provides a way to iterate over a reader data until limit is reached, closes it when necessary. The data is consumed
 * via a Supplier, which performs required action on data
 */
public class WriterDataSupplier<R, I> {

    private WriteOperation<R> writeOperation;

    private Supplier<I> supplier;

    private RuntimeContainer container;

    /**
     * @param writeOperation instance of the WriteOperation expecting result of type <R>.
     * @param supplier the callback to provide the writer records of type <I>.
     */
    public WriterDataSupplier(WriteOperation<R> writeOperation, Supplier<I> supplier, RuntimeContainer container) {
        this.writeOperation = writeOperation;
        this.supplier = supplier;
        this.container = container;
    }

    /**
     * Start writing data obtained from the supplier. This takes care of creating the initializing the
     * WriterOperation, creating a single writer, then starting and closing the writer and finally finalizing the
     * WriterOperation.<br>
     * The writer in initialized with a random string id.<br>
     * This will throw a {@link TalendRuntimeException} with {@link ComponentsErrorCode#IO_EXCEPTION} code in case of
     * writer error.
     */
    public void writeData() {
        writeOperation.initialize(container);
        Writer<R> writer = writeOperation.createWriter(container);
        try {
            writer.open(RandomStringUtils.random(12));
            try {
                I ir = supplier.get();
                if (ir != null) {
                    do {
                        writer.write(ir);
                    } while ((ir = supplier.get()) != null);
                } // supplier ended
            } finally {
                R result = writer.close();
                writeOperation.finalize(Collections.singleton(result), container);
            }
        } catch (IOException e) {
            // TODO create a write exception code.
            throw TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).create();
        }
    }

}
