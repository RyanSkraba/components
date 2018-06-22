package org.talend.components.simplefileio.runtime.hadoop.csv;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class CSVFileSplit extends FileSplit {

  /**
   * store the split index information about single file, we need it for checking if it's the first split
   *
   */
  private int index = 0;

  public CSVFileSplit() {
  }

  public CSVFileSplit(Path file, long start, long length, int index, String[] hosts) {
    super(file, start, length, hosts);
    this.index = index;
  }

  public CSVFileSplit(Path file, long start, long length, int index, String[] hosts, String[] inMemoryHosts) {
    super(file, start, length, hosts, inMemoryHosts);
    this.index = index;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    super.write(out);
    out.writeInt(index);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    super.readFields(in);
    index = in.readInt();
  }

  public int getIndex() {
    return index;
  }
}
