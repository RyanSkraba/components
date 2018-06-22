package org.talend.components.simplefileio.runtime.hadoop.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.StopWatch;

/**
 * A CSV Input format which can set the header line to skip some line. the
 * header line number should be a small number, if a big one even near by the
 * end of the file, it mean we process the whole CSV in one task, no meaning for
 * map reduce way.
 * 
 */
public class CSVFileInputFormat extends org.apache.hadoop.mapreduce.lib.input.FileInputFormat<LongWritable, BytesWritable> {

  public static String TALEND_ENCODING = "talend_encoding";
  
  public static String TALEND_TEXT_ENCLOSURE = "talend_text_enclosure";
  public static String TALEND_ESCAPE = "talend_escape";
  // not in the design
  public static String TALEND_ROW_DELIMITED = "talend_row_delimited";

  public static String TALEND_HEADER = "talend_header";

  private static final Log LOG = LogFactory.getLog(CSVFileInputFormat.class);

  private static final double SPLIT_SLOP = 1.1;

  @Override
  public CSVFileRecordReader createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
    String delimiter = context.getConfiguration().get(TALEND_ROW_DELIMITED);
    String encoding = context.getConfiguration().get(TALEND_ENCODING);

    String textEnclosure = context.getConfiguration().get(TALEND_TEXT_ENCLOSURE);
    String escapeChar = context.getConfiguration().get(TALEND_ESCAPE);

    Character te = null;
    Character ec = null;

    if (textEnclosure != null && !textEnclosure.isEmpty()) {
      te = textEnclosure.charAt(0);
    }

    if (escapeChar != null && !escapeChar.isEmpty()) {
      ec = escapeChar.charAt(0);
    }

    return createRecordReader(delimiter, encoding, te, ec);
  }

  private CSVFileRecordReader createRecordReader(final String rowDelimiter, final String encoding, final Character textEnclosure, final Character escapeChar) throws IOException {
    return new CSVFileRecordReader(rowDelimiter, encoding, textEnclosure, escapeChar);
  }

  private long caculateSkipLength(FileStatus file, JobContext job) throws IOException {
    long header = job.getConfiguration().getLong(TALEND_HEADER, 0l);
    String rowDelimiter = job.getConfiguration().get(TALEND_ROW_DELIMITED);
    String encoding = job.getConfiguration().get(TALEND_ENCODING);

    if (header < 1) {
      return 0l;
    }

    try (CSVFileRecordReader reader = this.createRecordReader(rowDelimiter, encoding, null, null)) {
      // TODO check if right for compress especially
      return reader.skipHeader(file, header, job);
    }
  }

  @Override
  public List<InputSplit> getSplits(JobContext job) throws IOException {
    StopWatch sw = new StopWatch().start();

    long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
    long maxSize = getMaxSplitSize(job);

    // generate splits
    List<InputSplit> splits = new ArrayList<InputSplit>();
    List<FileStatus> files = listStatus(job);
    for (FileStatus file : files) {
      Path path = file.getPath();
      long length = file.getLen();

      if (length != 0) {
        long skipLength = caculateSkipLength(file, job);

        BlockLocation[] blkLocations;
        if (file instanceof LocatedFileStatus) {
          blkLocations = ((LocatedFileStatus) file).getBlockLocations();
        } else {
          FileSystem fs = path.getFileSystem(job.getConfiguration());
          blkLocations = fs.getFileBlockLocations(file, 0, length);
        }

        int splitIndex = 0;

        if (isSplitable(job, path)) {
          long blockSize = file.getBlockSize();
          long splitSize = computeSplitSize(blockSize, minSize, maxSize);

          long bytesRemaining = length - skipLength;
          while (((double) bytesRemaining) / splitSize > SPLIT_SLOP) {
            long offset = length - bytesRemaining;
            int blkIndex = getBlockIndex(blkLocations, offset);
            splits.add(makeSplit(path, offset, splitSize, splitIndex++, blkLocations[blkIndex].getHosts(), blkLocations[blkIndex].getCachedHosts()));
            bytesRemaining -= splitSize;
          }

          if (bytesRemaining != 0) {
            long offset = length - bytesRemaining;
            int blkIndex = getBlockIndex(blkLocations, offset);
            splits.add(makeSplit(path, offset, bytesRemaining, splitIndex++, blkLocations[blkIndex].getHosts(), blkLocations[blkIndex].getCachedHosts()));
          }
        } else { // not splitable
          splits.add(makeSplit(path, skipLength, length - skipLength, splitIndex++, blkLocations[0].getHosts(), blkLocations[0].getCachedHosts()));
        }
      } else {
        // Create empty hosts array for zero length files
        splits.add(makeSplit(path, 0, length, 0, new String[0]));
      }
    }
    // Save the number of input files for metrics/loadgen
    job.getConfiguration().setLong(NUM_INPUT_FILES, files.size());

    sw.stop();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Total # of splits generated by getSplits: " + splits.size() + ", TimeTaken: " + sw.now(TimeUnit.MILLISECONDS));
    }
    return splits;
  }

  @Override
  protected boolean isSplitable(JobContext context, Path filename) {
    String text_enclosure = context.getConfiguration().get(TALEND_TEXT_ENCLOSURE);
    String talend_escape = context.getConfiguration().get(TALEND_ESCAPE);

    if ((text_enclosure != null && !text_enclosure.isEmpty()) || (talend_escape != null && !talend_escape.isEmpty())) {
      return false;
    }

    final CompressionCodec codec = new CompressionCodecFactory(context.getConfiguration()).getCodec(filename);
    if (null == codec) {
      return true;
    }
    return codec instanceof SplittableCompressionCodec;
  }

  @Override
  protected List<FileStatus> listStatus(JobContext job) throws IOException {
    // TODO consider if filter the sub dir
    return super.listStatus(job);
  }

  protected InputSplit makeSplit(Path file, long start, long length, int splitIndex, String[] hosts) {
    return new CSVFileSplit(file, start, length, splitIndex, hosts);
  }

  protected InputSplit makeSplit(Path file, long start, long length, int splitIndex, String[] hosts, String[] inMemoryHosts) {
    return new CSVFileSplit(file, start, length, splitIndex, hosts, inMemoryHosts);
  }

}
