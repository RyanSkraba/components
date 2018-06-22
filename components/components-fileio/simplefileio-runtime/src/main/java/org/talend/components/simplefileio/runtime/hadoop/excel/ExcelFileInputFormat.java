package org.talend.components.simplefileio.runtime.hadoop.excel;

import java.io.IOException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ExcelFileInputFormat extends org.apache.hadoop.mapreduce.lib.input.FileInputFormat<Void, IndexedRecord> {

  public static String TALEND_ENCODING = "talend_excel_encoding";
  
  public static String TALEND_EXCEL_SHEET_NAME = "talend_excel_sheet_name";
  
  public static String TALEND_HEADER = "talend_excel_header";
  
  public static String TALEND_FOOTER = "talend_excel_footer";
  
  public static String TALEND_EXCEL_FORMAT = "talend_excel_format";
  
  public static String TALEND_EXCEL_LIMIT = "talend_excel_limit";

  private static final Log LOG = LogFactory.getLog(ExcelFileInputFormat.class);

  @Override
  public RecordReader<Void, IndexedRecord> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
    String encoding = context.getConfiguration().get(TALEND_ENCODING);
    String sheet = context.getConfiguration().get(TALEND_EXCEL_SHEET_NAME);
    long header = context.getConfiguration().getLong(TALEND_HEADER, 0l);
    long footer = context.getConfiguration().getLong(TALEND_FOOTER, 0l);
    String excelFormat = context.getConfiguration().get(TALEND_EXCEL_FORMAT, "EXCEL2007");
    long limit = context.getConfiguration().getLong(TALEND_EXCEL_LIMIT, -1);
    
    if("EXCEL2007".equals(excelFormat)) {
      return new Excel2007FileRecordReader(sheet, header, footer, limit);
    } else if("EXCEL97".equals(excelFormat)) {
      return new Excel97FileRecordReader(sheet, header, footer, limit);
    } else if("HTML".equals(excelFormat)) {
      return new ExcelHTMLFileRecordReader(encoding, header, footer, limit);
    }
    
    throw new IOException("not a valid excel format");
  }

  @Override
  protected boolean isSplitable(JobContext context, Path filename) {
      return false;
  }

}
