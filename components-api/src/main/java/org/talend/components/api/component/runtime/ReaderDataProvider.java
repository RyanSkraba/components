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
import java.util.NoSuchElementException;

import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.java8.Consumer;

/**
 * Provides a way to iterate over a reader data until limit is reached, closes it when necessary. 
 * The data is consumed via a Consumer, which performs required action on data
 */
public class ReaderDataProvider<T> {

    private Reader<T> reader;

    private int limit;

    private Consumer<T> consumer;

    /**
     * @param reader, instance of the reader.
     * @param limit the number of record to read
     * @param consumer the callback to provide the readers records of type <T>.
     */
    public ReaderDataProvider(Reader<T> reader, int limit, Consumer<T> consumer) {
        this.reader = reader;
        this.limit = limit;
        this.consumer = consumer;
    }

    /**
     * start retrieving the data and pass it to the consumer.
     * This takes care of starting and closing the reader.
     * This will throw a {@link TalendRuntimeException} in case of reader error.
     */
    public void retrieveData() {
        try {
            try {
                boolean hasCurrent = reader.start();
                int count = 0;
                while (hasCurrent && count++ < limit) {
                    consumer.accept(reader.getCurrent());
                    hasCurrent = reader.advance();
                } // has ended
                return;
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).create();
        } catch (NoSuchElementException e) {
            throw TalendRuntimeException.createUnexpectedException(e);
        }
    }

}
