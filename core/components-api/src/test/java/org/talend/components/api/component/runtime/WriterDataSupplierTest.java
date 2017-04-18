package org.talend.components.api.component.runtime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Test;
import org.talend.daikon.java8.Supplier;


public class WriterDataSupplierTest {

    @Test
    public void testWriteDataSupplierWritedDataNull() throws IOException {
        Writer writerMock = mock(Writer.class);
        WriteOperation woMock = mock(WriteOperation.class);
        when(woMock.createWriter(null)).thenReturn(writerMock);
        new WriterDataSupplier<>(woMock, new Supplier<IndexedRecord>() {

            @Override
            public IndexedRecord get() {
                return null;
            }
        }, null).writeData();
        verify(woMock, times(1)).createWriter(null);
        verify(woMock, times(1)).initialize(null);
        verify(writerMock, times(1)).open(any(String.class));
        verify(writerMock, times(0)).write(null);
        verify(writerMock, times(1)).close();
        verify(woMock, times(1)).finalize(Collections.singleton(null), null);
    }

    @Test
    public void testWriteDataSupplierWritedData() throws IOException {
        final IndexedRecord ir = mock(IndexedRecord.class);
        Writer writerMock = mock(Writer.class);
        WriteOperation woMock = mock(WriteOperation.class);
        when(woMock.createWriter(null)).thenReturn(writerMock);
        when(writerMock.close()).thenReturn("foo");
        new WriterDataSupplier<>(woMock, new Supplier<IndexedRecord>() {

            int irCount = 1;

            @Override
            public IndexedRecord get() {
                switch (irCount++) {
                case 1:
                case 2:
                    return ir;
                default:
                    return null;
                }
            }
        }, null).writeData();
        verify(woMock, times(1)).createWriter(null);
        verify(woMock, times(1)).initialize(null);
        verify(writerMock, times(1)).open(any(String.class));
        verify(writerMock, times(2)).write(ir);
        verify(writerMock, times(1)).close();
        verify(woMock, times(1)).finalize(Collections.singleton("foo"), null);
    }

}
