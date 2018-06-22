package org.talend.components.simplefileio.runtime;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.apache.hadoop.io.BytesWritable;
import org.junit.Test;

/**
 * Unit tests for {@link SimpleRecordFormatCsvIO.ExtractCsvRecord}.
 */
public class ExtractCsvRecordTest {

    /** The DoFn under test. */
    //no quote, no escape
    private final DoFnTester<BytesWritable, IndexedRecord> fnBasic = DoFnTester.of(new SimpleRecordFormatCsvIO.ExtractCsvRecord(';', false, "UTF-8", null, null));
    
    //quote : ", no escape char
    private final DoFnTester<BytesWritable, IndexedRecord> fnQuote = DoFnTester.of(new SimpleRecordFormatCsvIO.ExtractCsvRecord(';', false, "UTF-8", '\"', null));
    
    //quote : ", escape char : \
    private final DoFnTester<BytesWritable, IndexedRecord> fnQuoteAndEscape = DoFnTester.of(new SimpleRecordFormatCsvIO.ExtractCsvRecord(';', false, "UTF-8", '\"', '\\'));
    
    //encoding only
    private final DoFnTester<BytesWritable, IndexedRecord> fnEncoding = DoFnTester.of(new SimpleRecordFormatCsvIO.ExtractCsvRecord(';', false, "GBK", null, null));

    public static String[] toArray(IndexedRecord record) {
        String[] fields = new String[record.getSchema().getFields().size()];
        for (int i = 0; i < fields.length; i++)
            fields[i] = record.get(i).toString();
        return fields;
    }

    public static List<String[]> toArrays(List<IndexedRecord> records) {
        List<String[]> out = new ArrayList<>(records.size());
        for (IndexedRecord r : records) {
            out.add(toArray(r));
        }
        return out;
    }

    public static void assertLine(String msg, DoFnTester<BytesWritable, IndexedRecord> fn, String inputLine, String... expected)
            throws Exception {
        byte[] bytes = inputLine.getBytes("UTF-8");
        
        BytesWritable bsw = new BytesWritable(bytes);
        
        assertThat(msg + ":" + inputLine, toArray(fn.processBundle(bsw).get(0)), arrayContaining(expected));
    }
    
    @Test
    public void testEncoding() throws Exception {
        byte[] bytes = "王伟;北京;Talend".getBytes("GBK");
        
        BytesWritable bsw = new BytesWritable(bytes);
        
        assertThat("encoding", toArray(fnEncoding.processBundle(bsw).get(0)), arrayContaining(new String[]{ "王伟", "北京", "Talend" }));
        
        bsw = new BytesWritable();

        // Empty lines are skipped
        assertThat(fnBasic.processBundle(bsw), hasSize(0));
    }

    @Test
    public void testBasic() throws Exception {
        assertLine("noQuoteOrEscape", fnBasic, "a;b;c", new String[]{ "a", "b", "c" });
        assertLine("noQuoteOrEscape", fnBasic, "\"a\";b;c", new String[]{ "\"a\"", "b", "c" });
        assertLine("noQuoteOrEscape", fnBasic, "a;\"b\";c", new String[]{ "a", "\"b\"", "c" });
        assertLine("noQuoteOrEscape", fnBasic, "a;b;\"c\"", new String[]{ "a", "b", "\"c\"" });
        assertLine("noQuoteOrEscape", fnBasic, "\"a\";\"b\";c", new String[]{ "\"a\"", "\"b\"", "c" });
        assertLine("noQuoteOrEscape", fnBasic, "a;\"b\";\"c\"", new String[]{ "a", "\"b\"", "\"c\"" });
        assertLine("noQuoteOrEscape", fnBasic, "\"a\";b;\"c\"", new String[]{ "\"a\"", "b", "\"c\"" });
        assertLine("noQuoteOrEscape", fnBasic, "\"a\";\"b\";\"c\"", new String[]{ "\"a\"", "\"b\"", "\"c\"" });
        
        BytesWritable bsw = new BytesWritable();

        // Empty lines are skipped
        assertThat(fnBasic.processBundle(bsw), hasSize(0));

        // TODO: Error when three quotes
        // assertLine("basic", fnBasic, "\"\"\"", "\"");
    }
    
    @Test
    public void testQuote() throws Exception {
        //the schema number have to be the same now
        assertLine("quote", fnQuote, "a;b;c", new String[]{ "a", "b", "c" });
        assertLine("quote", fnQuote, "\"a\";b;c", new String[]{ "a", "b", "c" });
        assertLine("quote", fnQuote, "a;\"b\";c", new String[]{ "a", "b", "c" });
        assertLine("quote", fnQuote, "a;b;\"c\"", new String[]{ "a", "b", "c" });
        assertLine("quote", fnQuote, "\"a\";\"b\";c", new String[]{ "a", "b", "c" });
        assertLine("quote", fnQuote, "a;\"b\";\"c\"", new String[]{ "a", "b", "c" });
        assertLine("quote", fnQuote, "\"a\";b;\"c\"", new String[]{ "a", "b", "c" });
        assertLine("quote", fnQuote, "\"a\";\"b\";\"c\"", new String[]{ "a", "b", "c" });
      
        assertLine("quote", fnQuote, "\"\";;", new String[]{"", "", ""});
        assertLine("quote", fnQuote, "\"\";\"\";\"\"", new String[]{"", "", ""});
        assertLine("quote", fnQuote, "\";\";\";\";\";\"", new String[]{";", ";", ";"});
        assertLine("quote", fnQuote, "\" \";\" \";\" \"", new String[]{ " ", " ", " " });
        assertLine("quote", fnQuote, "\"\"\"\";\"\"\"\";\"\"\"\"", new String[]{ "\"", "\"", "\"" });
        
        assertLine("quote", fnQuote, "\\;;", new String[]{ "\\", "","" });
        assertLine("quote", fnQuote, "\\;\\;\\", new String[]{ "\\", "\\", "\\" });
        
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"\r\n\";\"\n\";\";\"", new String[]{ "\r\n", "\n", ";" });
        
        BytesWritable bsw = new BytesWritable();

        // Empty lines are skipped
        assertThat(fnQuote.processBundle(bsw), hasSize(0));
    }
    
    @Test
    public void testQuoteAndEscape() throws Exception {
        //the schema number have to be the same now
        assertLine("quoteAndEscape", fnQuoteAndEscape, "a;b;c", new String[]{ "a", "b", "c" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"a\";b;c", new String[]{ "a", "b", "c" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "a;\"b\";c", new String[]{ "a", "b", "c" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "a;b;\"c\"", new String[]{ "a", "b", "c" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"a\";\"b\";c", new String[]{ "a", "b", "c" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "a;\"b\";\"c\"", new String[]{ "a", "b", "c" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"a\";b;\"c\"", new String[]{ "a", "b", "c" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"a\";\"b\";\"c\"", new String[]{ "a", "b", "c" });
      
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"\";;", new String[]{"", "", ""});
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"\";\"\";\"\"", new String[]{"", "", ""});
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\";\";\";\";\";\"", new String[]{";", ";", ";"});
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\" \";\" \";\" \"", new String[]{ " ", " ", " " });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"\"\"\";\"\"\"\";\"\"\"\"", new String[]{ "\"", "\"", "\"" });
        
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\\\\;;", new String[]{ "\\", "","" });
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\\\\;\\\\;\\\\", new String[]{ "\\", "\\", "\\" });
        
        assertLine("quoteAndEscape", fnQuoteAndEscape, "\"\\\"\";\"\n\";\";\"", new String[]{ "\"", "\n", ";" });
        
        BytesWritable bsw = new BytesWritable();

        // Empty lines are skipped
        assertThat(fnQuote.processBundle(bsw), hasSize(0));
    }

}
