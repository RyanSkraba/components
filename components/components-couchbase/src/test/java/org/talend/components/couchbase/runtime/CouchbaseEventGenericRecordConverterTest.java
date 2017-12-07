package org.talend.components.couchbase.runtime;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.daikon.avro.converter.IndexedRecordConverter.UnmodifiableAdapterException;

import com.couchbase.client.dcp.message.MessageUtil;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.ByteBufUtil;

public class CouchbaseEventGenericRecordConverterTest {

    private static final byte OFFSET = (byte) 0x05;

    private static final short LENGTH = (short) 10;

    private CouchbaseEventGenericRecordConverter converter;
    private Schema schema;

    @Before
    public void setup() {
        schema = SchemaBuilder.builder().record("record").fields().endRecord();
        converter = new CouchbaseEventGenericRecordConverter(schema);
    }

    @Test
    public void testGetDatumClass() {
        Assert.assertEquals(ByteBuf.class, converter.getDatumClass());
    }

    @Test(expected = UnmodifiableAdapterException.class)
    public void testConvertToDatum() {
        // This test seems to be fake. Also implementation should be more specific for writing into datasource.
        converter.convertToDatum(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertToAvroIllegalValue() {
        converter.convertToAvro(ByteBufUtil.threadLocalDirectBuffer());
    }

    @Test
    public void testConvertToAvroMutation() {
        ByteBuf buffer = Mockito.mock(ByteBuf.class);

        // Mocking key object
        Mockito.when(buffer.getByte(0)).thenReturn(MessageUtil.MAGIC_REQ);
        Mockito.when(buffer.getByte(1)).thenReturn(MessageUtil.DCP_MUTATION_OPCODE);
        Mockito.when(buffer.getByte(4)).thenReturn(OFFSET);
        Mockito.when(buffer.getShort(2)).thenReturn(LENGTH);
        ByteBuf key = Mockito.mock(ByteBuf.class);
        Mockito.when(key.readableBytes()).thenReturn(4);
        Mockito.when(buffer.slice(29, 10)).thenReturn(key);

        // Mocking content object.
        Mockito.when(buffer.getByte(4)).thenReturn(OFFSET);
        Mockito.when(buffer.getShort(2)).thenReturn(LENGTH);
        Mockito.when(buffer.getInt(8)).thenReturn(100);
        ByteBuf content = Mockito.mock(ByteBuf.class);
        // Content byte array length.
        Mockito.when(content.readableBytes()).thenReturn(85);
        Mockito.when(buffer.slice(39, 85)).thenReturn(content);

        IndexedRecord record = converter.convertToAvro(buffer);

        Assert.assertEquals(schema, record.getSchema());

        assertIndexedRecordResult(record, "mutation", new byte[85]);

    }

    @Test
    public void testConvertToAvroDeletion() {
        ByteBuf buffer = Mockito.mock(ByteBuf.class);

        // Mocking key object
        Mockito.when(buffer.getByte(0)).thenReturn(MessageUtil.MAGIC_REQ);
        Mockito.when(buffer.getByte(1)).thenReturn(MessageUtil.DCP_DELETION_OPCODE);
        Mockito.when(buffer.getByte(4)).thenReturn(OFFSET);
        Mockito.when(buffer.getShort(2)).thenReturn(LENGTH);
        ByteBuf key = Mockito.mock(ByteBuf.class);
        Mockito.when(key.readableBytes()).thenReturn(4);
        Mockito.when(buffer.slice(29, 10)).thenReturn(key);

        IndexedRecord record = converter.convertToAvro(buffer);

        assertIndexedRecordResult(record, "deletion", null);

    }

    @Test
    public void testConvertToAvroExpiration() {
        ByteBuf buffer = Mockito.mock(ByteBuf.class);

        // Mocking key object
        Mockito.when(buffer.getByte(0)).thenReturn(MessageUtil.MAGIC_REQ);
        Mockito.when(buffer.getByte(1)).thenReturn(MessageUtil.DCP_EXPIRATION_OPCODE);
        Mockito.when(buffer.getByte(4)).thenReturn(OFFSET);
        Mockito.when(buffer.getShort(2)).thenReturn(LENGTH);
        ByteBuf key = Mockito.mock(ByteBuf.class);
        Mockito.when(key.readableBytes()).thenReturn(4);
        Mockito.when(buffer.slice(29, 10)).thenReturn(key);

        IndexedRecord record = converter.convertToAvro(buffer);

        assertIndexedRecordResult(record, "expiration", null);

    }

    // This assertion must be improved later with covering real data.
    private void assertIndexedRecordResult(IndexedRecord record, String event, byte[] content) {
        Assert.assertEquals(event, record.get(0));
        Assert.assertEquals((short) 0, record.get(1));
        Assert.assertEquals((long) 0, record.get(3));
        Assert.assertEquals((long) 0, record.get(4));
        Assert.assertEquals((long) 0, record.get(5));
        Assert.assertEquals(0, record.get(6));
        Assert.assertEquals(0, record.get(7));
        Assert.assertEquals(0, record.get(8));
        if (content != null) {
            Assert.assertEquals(content.length, ((byte[]) record.get(9)).length);
        }

    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testConvertToAvroFailedIllegalRecordIndex() {
        ByteBuf buffer = Mockito.mock(ByteBuf.class);

        // Mocking key object
        Mockito.when(buffer.getByte(0)).thenReturn(MessageUtil.MAGIC_REQ);
        Mockito.when(buffer.getByte(1)).thenReturn(MessageUtil.DCP_EXPIRATION_OPCODE);
        Mockito.when(buffer.getByte(4)).thenReturn(OFFSET);
        Mockito.when(buffer.getShort(2)).thenReturn(LENGTH);
        ByteBuf key = Mockito.mock(ByteBuf.class);
        Mockito.when(key.readableBytes()).thenReturn(4);
        Mockito.when(buffer.slice(29, 10)).thenReturn(key);

        IndexedRecord record = converter.convertToAvro(buffer);

        record.get(10);
    }

    @Test (expected = UnmodifiableAdapterException.class)
    public void testConvertToAvroFailedToChangeRecord() {
        ByteBuf buffer = Mockito.mock(ByteBuf.class);

        // Mocking key object
        Mockito.when(buffer.getByte(0)).thenReturn(MessageUtil.MAGIC_REQ);
        Mockito.when(buffer.getByte(1)).thenReturn(MessageUtil.DCP_EXPIRATION_OPCODE);
        Mockito.when(buffer.getByte(4)).thenReturn(OFFSET);
        Mockito.when(buffer.getShort(2)).thenReturn(LENGTH);
        ByteBuf key = Mockito.mock(ByteBuf.class);
        Mockito.when(key.readableBytes()).thenReturn(4);
        Mockito.when(buffer.slice(29, 10)).thenReturn(key);

        IndexedRecord record = converter.convertToAvro(buffer);

        record.put(0, "Update is not supported");
    }
}
