package org.talend.components.simplefileio.runtime.hadoop.csv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CodecPool;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.Decompressor;
import org.apache.hadoop.io.compress.SplitCompressionInputStream;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CompressedSplitLineReader;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.SplitLineReader;

public class CSVFileRecordReader extends RecordReader<LongWritable, BytesWritable> {
  private static final Log LOG = LogFactory.getLog(CSVFileRecordReader.class);
  public static final String MAX_LINE_LENGTH = "mapreduce.input.linerecordreader.line.maxlength";

  private long start;
  private long pos;
  private long end;
  private SplitLineReader in;
  private FSDataInputStream fileIn;
  private Seekable filePosition;
  private int maxLineLength;
  private LongWritable key;

  private Text value;
  private BytesWritable bytesValue;

  private boolean isCompressedInput;
  private Decompressor decompressor;

  private String recordDelimiter;
  private byte[] recordDelimiterBytes;

  private String encoding = "UTF-8";

  // now only one char is valid, mutli characters is not be considered.
  private Character textEnclosure;
  private Character escapeChar;

  private boolean isComplexCSV = false;

  public CSVFileRecordReader() {
  }

  public CSVFileRecordReader(String recordDelimiter, String encoding, Character textEnclosure, Character escapeChar) throws UnsupportedEncodingException {
    this.recordDelimiter = recordDelimiter;
    if (recordDelimiter != null) {
      this.recordDelimiterBytes = recordDelimiter.getBytes(encoding);
    }
    this.encoding = encoding;

    this.textEnclosure = textEnclosure;
    this.escapeChar = escapeChar;

    this.isComplexCSV = (textEnclosure != null) || (escapeChar != null);
  }

  public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
    FileSplit split = (FileSplit) genericSplit;
    Configuration job = context.getConfiguration();
    this.maxLineLength = job.getInt(MAX_LINE_LENGTH, Integer.MAX_VALUE);
    start = split.getStart();
    end = start + split.getLength();
    final Path file = split.getPath();

    // open the file and seek to the start of the split
    final FileSystem fs = file.getFileSystem(job);
    fileIn = fs.open(file);

    CompressionCodec codec = new CompressionCodecFactory(job).getCodec(file);
    if (null != codec) {
      isCompressedInput = true;
      decompressor = CodecPool.getDecompressor(codec);
      if (codec instanceof SplittableCompressionCodec) {
        final SplitCompressionInputStream cIn = ((SplittableCompressionCodec) codec).createInputStream(fileIn, decompressor, start, end, SplittableCompressionCodec.READ_MODE.BYBLOCK);
        in = new CompressedSplitLineReader(cIn, job, this.recordDelimiterBytes);
        start = cIn.getAdjustedStart();
        end = cIn.getAdjustedEnd();
        filePosition = cIn;
      } else {
        in = new SplitLineReader(codec.createInputStream(fileIn, decompressor), job, this.recordDelimiterBytes);
        filePosition = fileIn;
      }
    } else {
      fileIn.seek(start);
      in = new SplitLineReader(fileIn, job, this.recordDelimiterBytes);
      filePosition = fileIn;
    }

    // Support the header
    int splitIndex = 0;
    if (split instanceof CSVFileSplit) {
      splitIndex = ((CSVFileSplit) split).getIndex();
    }

    // If this is not the first split, we always throw away first record
    // because we always (except the last split) read one extra line in
    // next() method.
    if (splitIndex != 0) {
      start += in.readLine(new Text(), 0, maxBytesToConsume(start));
    }
    this.pos = start;
  }

  private int maxBytesToConsume(long pos) {
    return isCompressedInput ? Integer.MAX_VALUE : (int) Math.max(Math.min(Integer.MAX_VALUE, end - pos), maxLineLength);
  }

  private long getFilePosition() throws IOException {
    long retVal;
    if (isCompressedInput && null != filePosition) {
      retVal = filePosition.getPos();
    } else {
      retVal = pos;
    }
    return retVal;
  }

  private int skipUtfByteOrderMark() throws IOException {
    // Strip BOM(Byte Order Mark)
    // Text only support UTF-8, we only need to check UTF-8 BOM
    // (0xEF,0xBB,0xBF) at the start of the text stream.
    int newMaxLineLength = (int) Math.min(3L + (long) maxLineLength, Integer.MAX_VALUE);
    int newSize = in.readLine(value, newMaxLineLength, maxBytesToConsume(pos));
    // Even we read 3 extra bytes for the first line,
    // we won't alter existing behavior (no backwards incompat issue).
    // Because the newSize is less than maxLineLength and
    // the number of bytes copied to Text is always no more than newSize.
    // If the return size from readLine is not less than maxLineLength,
    // we will discard the current line and read the next line.
    pos += newSize;
    int textLength = value.getLength();
    byte[] textBytes = value.getBytes();
    if ((textLength >= 3) && (textBytes[0] == (byte) 0xEF) && (textBytes[1] == (byte) 0xBB) && (textBytes[2] == (byte) 0xBF)) {
      // find UTF-8 BOM, strip it.
      LOG.info("Found UTF-8 BOM and skipped it");
      textLength -= 3;
      newSize -= 3;
      if (textLength > 0) {
        // It may work to use the same buffer and not do the copyBytes
        textBytes = value.copyBytes();
        value.set(textBytes, 3, textLength);
      } else {
        value.clear();
      }
    }
    return newSize;
  }

  /**
   * call by input format to skip the fixed number header line and get the new
   * start location
   * 
   * FIXME the logic is the same with the studio one, but find a bug : can't
   * work well with escape char and text enclosure when newline characters
   * exists in one CSV column. As if the escape char and text enclosure exists,
   * only process by one map, maybe for fixing it, in future, we should provide
   * another input format for them, not split and more easy
   * 
   * don't support the header in multi files
   */
  public long skipHeader(FileStatus fss, long header, JobContext context) throws IOException {
    Configuration job = context.getConfiguration();
    
    start = 0;
    end = fss.getLen();
    
    Path file = fss.getPath();
    final FileSystem fs = file.getFileSystem(job);
    fileIn = fs.open(file);
    
    CompressionCodec codec = new CompressionCodecFactory(job).getCodec(file);
    if (null != codec) {
      isCompressedInput = true;
      decompressor = CodecPool.getDecompressor(codec);
      if (codec instanceof SplittableCompressionCodec) {
        final SplitCompressionInputStream cIn = ((SplittableCompressionCodec) codec).createInputStream(fileIn, decompressor, start, end, SplittableCompressionCodec.READ_MODE.BYBLOCK);
        in = new CompressedSplitLineReader(cIn, job, this.recordDelimiterBytes);
        start = cIn.getAdjustedStart();
        end = cIn.getAdjustedEnd();
        filePosition = cIn;
      } else {
        in = new SplitLineReader(codec.createInputStream(fileIn, decompressor), job, this.recordDelimiterBytes);
        filePosition = fileIn;
      }
    } else {
      fileIn.seek(start);
      in = new SplitLineReader(fileIn, job, this.recordDelimiterBytes);
      filePosition = fileIn;
    }
    
    this.pos = start;
    
    value = new Text();
    
    int newSize = 0;

    for (int i = 0; i < header; i++) {
      if (pos == 0) {
        newSize = skipUtfByteOrderMark();
      } else {
        newSize = in.readLine(value, 0, maxBytesToConsume(pos));
        pos += newSize;
      }
    }

    if (newSize == 0) {
      throw new RuntimeException("header value exceed the limit of the file");
    }

    return getFilePosition();
  }

  public boolean nextKeyValue() throws IOException {
    if (!isComplexCSV || (textEnclosure == null)) {//escape char also can escape newLine?not common, textEnclosure is more common for that case.
      boolean hasNext = next();

      if(hasNext) {
        // TODO not use value.getBytes() directly, as not sure value.getLength() == value.getBytes().length, will use copyBytes
        byte[] bytes = java.util.Arrays.copyOfRange(value.getBytes(), 0, value.getLength());
        bytesValue = new BytesWritable(bytes);
      } else {
        bytesValue = null;
      }

      return hasNext;
    }

    // the code below only execute in one single map, no need to consider multi
    // split

    String currentLine = null;
    int numberOfTextEnclosureChar = 0;

    // We are looping on the input until we find a pair number of
    // text enclosure character.
    // It allow the handle schema like " 'abc\ndef' " (with "'" as
    // text enclosure character)

    boolean hasNext = true;
    do {
      hasNext = next();
      if (hasNext) {
        // There is still data to process ie we are not at the
        // end of a logical line
        String currentSubline = new String(java.util.Arrays.copyOfRange(value.getBytes(), 0, value.getLength()), encoding);

        // Get the number of escape character on the current
        // substring,
        // in order to check if we have or not a complete column
        for (int index = currentSubline.indexOf(textEnclosure); index >= 0; index = currentSubline.indexOf(textEnclosure, index + 1)) {
          if ((index == 0) || (escapeChar==null) || (currentSubline.charAt(index - 1) != escapeChar)) {
            numberOfTextEnclosureChar++;
          }

        }
        if (currentLine == null) {
          currentLine = currentSubline;
        } else {
          currentLine += recordDelimiter + currentSubline;
        }
      }
    } while ((hasNext) && (numberOfTextEnclosureChar % 2 != 0));

    hasNext = (numberOfTextEnclosureChar % 2 != 0) || hasNext;
    
    if(hasNext) {
      byte[] bytes = currentLine.getBytes(encoding);
      bytesValue = new BytesWritable(bytes);
    }
    
    return hasNext;
  }

  private boolean next() throws IOException {
    if (key == null) {
      key = new LongWritable();
    }
    key.set(pos);
    if (value == null) {
      value = new Text();
    }
    int newSize = 0;
    // We always read one extra line, which lies outside the upper
    // split limit i.e. (end - 1)
    while (getFilePosition() <= end || in.needAdditionalRecordAfterSplit()) {
      if (pos == 0) {
        newSize = skipUtfByteOrderMark();
      } else {
        newSize = in.readLine(value, maxLineLength, maxBytesToConsume(pos));
        pos += newSize;
      }

      if ((newSize == 0) || (newSize < maxLineLength)) {
        break;
      }

      // line too long. try again
      LOG.info("Skipped line of size " + newSize + " at pos " + (pos - newSize));
    }
    if (newSize == 0) {
      key = null;
      value = null;
      return false;
    } else {
      return true;
    }
  }

  @Override
  public LongWritable getCurrentKey() {
    return key;
  }

  @Override
  public BytesWritable getCurrentValue() {
    return bytesValue;
  }

  /**
   * Get the progress within the split
   */
  public float getProgress() throws IOException {
    if (start == end) {
      return 0.0f;
    } else {
      return Math.min(1.0f, (getFilePosition() - start) / (float) (end - start));
    }
  }

  public synchronized void close() throws IOException {
    try {
      if (in != null) {
        in.close();
      }
    } finally {
      if (decompressor != null) {
        CodecPool.returnDecompressor(decompressor);
        decompressor = null;
      }
    }
  }
}