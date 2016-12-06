// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.time.Instant;
import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.java8.Consumer;

public class ReaderDataProviderTest {

    @Test
    public void testProviderEmptyReader() throws IOException {
        Reader readerMock = mock(Reader.class);
        when(readerMock.start()).thenReturn(false);
        Consumer consumer = mock(Consumer.class);
        ReaderDataProvider<Object> readerDataProvider = new ReaderDataProvider<>(readerMock, 100, consumer);
        readerDataProvider.retrieveData();
        verify(consumer, times(0)).accept(any());
        verify(readerMock, times(1)).close();
    }

    @Test
    public void testReaderDataProviderWithRecords() throws IOException {
        Reader<String> reader = spy(new OneTwoReader());
        Consumer consumer = mock(Consumer.class);
        ReaderDataProvider<String> readerDataProvider = new ReaderDataProvider<>(reader, 100, consumer);
        readerDataProvider.retrieveData();
        verify(consumer).accept("1");
        verify(consumer).accept("2");
        verify(consumer, times(2)).accept(any());
        verify(reader, times(1)).close();
    }

    @Test
    public void testReaderDataProviderWithLimitTo0() throws IOException {
        Reader<String> reader = spy(new OneTwoReader());
        Consumer consumer = mock(Consumer.class);
        ReaderDataProvider<String> readerDataProvider = new ReaderDataProvider<>(reader, 0, consumer);
        readerDataProvider.retrieveData();
        verify(consumer, never()).accept(any());
        verify(reader, times(1)).close();
    }

    @Test
    public void testReaderDataProviderWithLimitTo1() throws IOException {
        Reader<String> reader = spy(new OneTwoReader());
        Consumer consumer = mock(Consumer.class);
        ReaderDataProvider<String> readerDataProvider = new ReaderDataProvider<>(reader, 1, consumer);
        readerDataProvider.retrieveData();
        verify(consumer).accept("1");
        verify(consumer, times(1)).accept(any());
        verify(reader, times(1)).close();
    }

    @Test
    public void testReaderDataProviderWithException() throws IOException {
        Reader<String> reader = mock(Reader.class);
        Consumer consumer = mock(Consumer.class);
        ReaderDataProvider<String> readerDataProvider = new ReaderDataProvider<>(reader, 100, consumer);

        // reader start throws an IOE
        when(reader.start()).thenThrow(new IOException());
        try {
            readerDataProvider.retrieveData();
            fail("the code above should have thrown an exception");
        } catch (TalendRuntimeException tre) {
            // expected exception
            verify(reader, times(1)).close();
        }

        // reader getCurrent throws an IOE
        reset(reader);
        when(reader.start()).thenReturn(true);
        when(reader.getCurrent()).thenThrow(new NoSuchElementException());
        try {
            readerDataProvider.retrieveData();
            fail("the code above should have thrown an exception");
        } catch (TalendRuntimeException tre) {
            // expected exception
            verify(reader, times(1)).close();
        }

        // reader close throws an IOE
        reset(reader);
        when(reader.start()).thenReturn(false);
        doThrow(new IOException()).when(reader).close();
        try {
            readerDataProvider.retrieveData();
            fail("the code above should have thrown an exception");
        } catch (TalendRuntimeException tre) {
            // expected exception
        }
    }

    public class OneTwoReader implements Reader<String> {

        Iterator<String> it = Arrays.asList("1", "2").iterator();

        private String current;

        @Override
        public boolean start() throws IOException {
            current = it.next();
            return true;
        }

        @Override
        public boolean advance() throws IOException {
            if (it.hasNext()) {
                current = it.next();
            } else {
                current = null;
            }
            return current != null;
        }

        @Override
        public String getCurrent() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current;
        }

        @Override
        public Instant getCurrentTimestamp() throws NoSuchElementException {
            return null;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public Source getCurrentSource() {
            return null;
        }

        @Override
        public Map<String, Object> getReturnValues() {
            return null;
        }

    }
}
