package org.talend.components.simplefileio.runtime.hadoop.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang.StringUtils;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.talend.components.simplefileio.runtime.hadoop.excel.streaming.StreamingReader;
import org.talend.daikon.avro.NameUtil;

import com.monitorjbl.xlsx.impl.StreamingRow;

public class Excel2007FileRecordReader extends RecordReader<Void, IndexedRecord> {
  private static final Log LOG = LogFactory.getLog(Excel2007FileRecordReader.class);

  private Workbook stream_workbook;
  
  private IndexedRecord value;

  private Decompressor decompressor;

  private String sheetName;
  private long header;
  private long footer;

  private long currentRow;
  private long endRow;
  
  private Iterator<Row> rowIterator;
  
  private long limit;

  public Excel2007FileRecordReader() {
  }

  public Excel2007FileRecordReader(String sheet, long header, long footer, long limit) throws UnsupportedEncodingException {
    this.sheetName = sheet;
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
      
      init4Excel2007(in, job, file);
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

  private void init4Excel2007(InputStream in, Configuration job, Path file) throws IOException {
    Sheet sheet = initStreamWorkbookAndSheet(in);

    endRow = Long.MAX_VALUE;
    if(footer > 0) {
      try {
        //read the whole file one time by stream for get the end row
        long rowNum = 0;
        for(Row row : sheet) {
          rowNum++;
        }
        
        endRow = rowNum - footer;
      } finally {
        if(stream_workbook != null) {
          stream_workbook.close();
          stream_workbook = null;
        }
      }
      
      //no data row, only footer, header, then exception notice
      if(header >= endRow) {
        throw new RuntimeException("no enough data row as header or footer value is too large, please reset them");
      }
      
      //recreate the stream sheet for the second time to read
      sheet = initStreamWorkbookAndSheet(this.createInputStream(job, file));
    }

    // skip header
    rowIterator = sheet.iterator();
    
    //we use it to fetch the schema
    Row headerRow = null;
    
    while ((header--) > 0 && rowIterator.hasNext()) {
      currentRow++;
      headerRow = rowIterator.next();
    }
    
    if(!rowIterator.hasNext()) {
      throw new RuntimeException("no enough data row as header or footer value is too large, please reset them");
    }
    
    //as only one task to process the excel as no split, so we can do that like this
    if(!ExcelUtils.isEmptyRow4Stream(headerRow)) {
      schema = createSchema(headerRow, false);
    }
  }

  private Sheet initStreamWorkbookAndSheet(InputStream in) {
    Sheet sheet = null;
    
    stream_workbook = StreamingReader.builder()
        .bufferSize(4096)
        .rowCacheSize(1)
        .open(in);

    try {
      if(StringUtils.isEmpty(this.sheetName)) {
        sheet = stream_workbook.getSheetAt(0);
      } else {
        sheet = stream_workbook.getSheet(this.sheetName);
      }
    } catch (Exception e) {
      //ignore it
    }
        
    if (sheet == null) {
      throw new RuntimeException("can't find the sheet : " + sheetName);
    }
    
    return sheet;
  }
  
  private static final String RECORD_NAME = "StringArrayRecord";

  private static final String FIELD_PREFIX = "field";
  
  private Schema createSchema(Row headerRow, boolean validName) {
    SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(RECORD_NAME).fields();
    
    if(headerRow!=null) {
      Set<String> existNames = new HashSet<String>();
      int index = 0;
      
      int cellNumber = getCellNumber(headerRow);
      
      for (int i = 0; i < cellNumber; i++) {
          Cell cell = headerRow.getCell(i);
          String fieldName = validName ? (cell == null ? StringUtils.EMPTY : cell.getStringCellValue()) : (FIELD_PREFIX + (i));
          
          String finalName = NameUtil.correct(fieldName, index++, existNames);
          existNames.add(finalName);
          
          fa = fa.name(finalName).type(Schema.create(Schema.Type.STRING)).noDefault();
      }
    }
    
    return fa.endRecord();
  }

  //stream interface adjust
  private int getCellNumber(Row headerRow) {
    Map<Integer, Cell> cellMap = ((StreamingRow)headerRow).getCellMap();
    
    int max = 0;
    boolean emptyRow = true;
    for(Map.Entry<Integer, Cell> entry : cellMap.entrySet()) {
      Integer key = entry.getKey();
      if(key != null) {
        emptyRow = false;
        max = Math.max(max, key);
      }
    }
    
    if(emptyRow) {
      return 0;
    }
    
    return max + 1;
  }

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
    
    return nextKeyValue4Excel2007();
  }
  
  private boolean nextKeyValue4Excel2007() throws IOException {
    if (!rowIterator.hasNext()) {
      return false;
    }

    currentRow++;

    Row row = rowIterator.next();
    
    if(ExcelUtils.isEmptyRow4Stream(row)) {
      //skip empty rows
      return next();
    }

    //if not fill the schema before as no header or invalid header, set it here and as no valid name as no header, so set a name like this : field1,field2,field3
    if(schema == null) {
      schema = createSchema(row, false);
    }
    value = new GenericData.Record(schema);
    
    List<Field> fields = schema.getFields();
    
    for (int i=0; i<fields.size(); i++) {
      Cell cell = row.getCell(i);
      String content = cell == null ? StringUtils.EMPTY : cell.getStringCellValue();
      value.put(i, content);
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
    try {
      if(stream_workbook != null) {
        stream_workbook.close();
        stream_workbook = null;
      }
    } finally {
      if (decompressor != null) {
        CodecPool.returnDecompressor(decompressor);
        decompressor = null;
      }
    }
  }
}