package org.talend.components.simplefileio.runtime.hadoop.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CodecPool;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.Decompressor;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.talend.daikon.avro.NameUtil;

public class ExcelHTMLFileRecordReader extends RecordReader<Void, IndexedRecord> {
  private static final Log LOG = LogFactory.getLog(ExcelHTMLFileRecordReader.class);

  private IndexedRecord value;

  private Decompressor decompressor;

  private String encoding = "UTF-8";

  private long header;
  private long footer;

  private long currentRow;
  private long endRow;
  
  private List<List<String>> rows;
  private Iterator<List<String>> htmlRowIterator;
  
  private long limit;

  public ExcelHTMLFileRecordReader() {
  }

  public ExcelHTMLFileRecordReader(String encoding, long header, long footer, long limit) throws UnsupportedEncodingException {
    this.encoding = encoding;
    this.header = header;
    this.footer = footer;
    this.limit = limit;
    this.limitCount = limit;
  }

  public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
    FileSplit split = (FileSplit) genericSplit;
    Configuration job = context.getConfiguration();

    final Path file = split.getPath();

    try {
      InputStream in = createInputStream(job, file);
      
      init4ExcelHtml(in);
    } catch (Exception e) {
      closeResource();
      throw e;
    }
  }

  private InputStream createInputStream(Configuration job, final Path file) throws IOException {
    final FileSystem fs = file.getFileSystem(job);
    InputStream in = fs.open(file);

    CompressionCodec codec = new CompressionCodecFactory(job).getCodec(file);
    if (null != codec) {
      decompressor = CodecPool.getDecompressor(codec);
      in = codec.createInputStream(in, decompressor);
    }
    return in;
  }

  private void init4ExcelHtml(InputStream in) {
    long limit4Parser = limit < 1 ? -1 : (header + footer + (limit + 2));
    try {
      rows = ExcelHtmlParser.getRows(in, this.encoding, limit4Parser);
    } finally {
      closeStream(in);
    }
    
    endRow = rows.size() - footer;
    //for html format, the first line is always the schema show, we don't read it always now, so header 1 or 0 both mean skip the schema row only.
    //TODO now we implement the header like "skip lines" which is clear name for the implement, need to consider what the header work for? for schema retrieve? or for skip lines only?
    header = Math.max(1, header);
    boolean isSchemaHeader = header < 2;
    
    //no data row, only footer, header, then exception notice
    if(header >= endRow) {
      throw new RuntimeException("no enough data row as header or footer value is too large, please reset them");
    }
    
    htmlRowIterator = rows.iterator();
    
    //we use it to fetch the schema
    List<String> headerRow = null;
    
    while ((header--) > 0 && htmlRowIterator.hasNext()) {
      currentRow++;
      headerRow = htmlRowIterator.next();
    }
    
    if(!htmlRowIterator.hasNext()) {
      throw new RuntimeException("no enough data row as header or footer value is too large, please reset them");
    }
    
    //as only one task to process the excel as no split, so we can do that like this
    if(isSchemaHeader && headerRow!=null && !headerRow.isEmpty()) {
      schema = createSchema(headerRow, true);
    }
  }
  
  private Schema createSchema(List<String> headerRow, boolean validName) {
    SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(RECORD_NAME).fields();
    
    if(headerRow!=null) {
      Set<String> existNames = new HashSet<String>();
      int index = 0;
      
      for (int i = 0; i < headerRow.size(); i++) {
          String fieldName = validName ? headerRow.get(i) : (FIELD_PREFIX + i);
          
          String finalName = NameUtil.correct(fieldName, index++, existNames);
          existNames.add(finalName);
          
          fa = fa.name(finalName).type(Schema.create(Schema.Type.STRING)).noDefault();
      }
    }
    
    return fa.endRecord();
  }
  
  private void closeStream(InputStream in) {
    try {
      in.close();
    } catch (IOException e) {
      LOG.warn("Failed to close the stream : " + e);
    }
  }
  
  private static final String RECORD_NAME = "StringArrayRecord";

  private static final String FIELD_PREFIX = "field";
  
  private Schema schema;
  
  private long limitCount = 0;
  
  public boolean nextKeyValue() throws IOException {
    boolean hasNext = next();
    
    if((limit > 0) && ((limitCount--) < 1)) {
      return false;
    }
    
    return hasNext;
  }

  private boolean next() throws IOException {
    if (currentRow >= endRow) {
      return false;
    }
    
    return nextKeyValue4ExcelHtml();
  }
  
  private boolean nextKeyValue4ExcelHtml() {
    if (!htmlRowIterator.hasNext()) {
      return false;
    }

    currentRow++;

    List<String> row = htmlRowIterator.next();

    //if not fill the schema before as no header or invalid header, set it here and as no valid name as no header, so set a name like this : field1,field2,field3
    if(schema == null) {
      schema = createSchema(row, false);
    }
    value = new GenericData.Record(schema);
   
    List<Field> fields = schema.getFields();
    
    for (int i=0;i<row.size();i++) {
      if(i<fields.size()) {
        value.put(i, row.get(i));
      }
    }
    
    return true;
  }

  @Override
  public Void getCurrentKey() {
    return null;
  }

  @Override
  public IndexedRecord getCurrentValue() {
    return value;
  }

  /**
   * Get the progress within the split, TODO not right in fact, the most time
   * for this is parsing excel file to object, not the reading object part
   */
  public float getProgress() throws IOException {
    return currentRow / (endRow - header);
  }

  public synchronized void close() throws IOException {
    closeResource();
  }

  private void closeResource() throws IOException {
    if (decompressor != null) {
      CodecPool.returnDecompressor(decompressor);
      decompressor = null;
    }
  }
}