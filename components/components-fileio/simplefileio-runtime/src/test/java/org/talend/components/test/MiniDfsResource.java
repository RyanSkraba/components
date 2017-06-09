// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSTestUtil;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.parquet.avro.AvroParquetReader;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.talend.components.simplefileio.runtime.utils.FileSystemUtil;

/**
 * Reusable for creating a {@link MiniDFSCluster} in a local, temporary directory.
 */
public class MiniDfsResource extends TemporaryFolder {

    public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz123456780".toCharArray();

    /**
     * Set to the current test being run.
     */
    private String testName = null;

    /**
     * Local test HDFS cluster.
     */
    private MiniDFSCluster miniHdfs = null;

    /**
     * Access to the HDFS FileSystem under test.
     */
    private FileSystem fs = null;

    /**
     * @param path the name of the file on the HDFS cluster
     * @param lines the lines to write to the file (with terminating end-of-lines).
     */
    public static String writeFile(FileSystem fs, String path, String... lines) throws IOException {
        try (PrintWriter w = new PrintWriter(fs.create(new Path(path)))) {
            for (String line : lines)
                w.println(line);
        }
        return DFSTestUtil.readFile(fs, new Path(path));
    }

    /**
     * Tests that a file on the HDFS cluster contains the given texts.
     *
     * @param path the name of the file on the HDFS cluster
     * @param expected the expected lines in the file (not including terminating end-of-lines).
     */
    public static void assertReadFile(FileSystem fs, String path, String... expected) throws IOException {
        Path p = new Path(path);
        if (fs.isFile(p)) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(fs.open(new Path(path))))) {
                for (String line : expected)
                    assertThat(r.readLine(), is(line));
                assertThat(r.readLine(), nullValue());
            }
        } else if (fs.isDirectory(p)) {
            HashSet<String> expect = new HashSet<>(Arrays.asList(expected));
            for (FileStatus fstatus : fs.listStatus(p)) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(fs.open(fstatus.getPath())))) {
                    String line = null;
                    while (null != (line = r.readLine()))
                        if (!expect.remove(line))
                            fail("Unexpected line: " + line);
                }
            }
            // Check before asserting for the message.
            if (expect.size() != 0)
                assertThat("Not all lines found: " + expect.iterator().next(), expect, hasSize(0));
        } else {
            fail("No such path: " + path);
        }
    }

    /**
     * Tests that a file on the HDFS cluster contains the given texts.
     *
     * @param path the name of the file on the HDFS cluster
     * @param expected the expected lines in the file (not including terminating end-of-lines).
     */
    public static void assertReadFile(String recordDelimiter, FileSystem fs, String path, String... expected) throws IOException {
        Path p = new Path(path);
        if (fs.isFile(p)) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(fs.open(new Path(path))))) {
                Scanner s = new Scanner(r).useDelimiter(recordDelimiter);
                for (String line : expected) {
                    assertThat(s.next(), is(line));
                }
                assertThat(s.hasNext(), is(false));
            }
        } else if (fs.isDirectory(p)) {
            HashSet<String> expect = new HashSet<>(Arrays.asList(expected));
            for (FileStatus fstatus : fs.listStatus(p)) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(fs.open(fstatus.getPath())))) {
                    Scanner s = new Scanner(r).useDelimiter(recordDelimiter);
                    String line = null;
                    while (s.hasNext()) {
                        line = s.next();
                        if (!expect.remove(line))
                            fail("Unexpected line: " + line);
                    }
                }
            }
            // Check before asserting for the message.
            if (expect.size() != 0)
                assertThat("Not all lines found: " + expect.iterator().next(), expect, hasSize(0));
        } else {
            fail("No such path: " + path);
        }
    }

    /**
     * Tests that how many files located in the folder
     *
     * @param folder the output folder
     * @param expected the expected number of the child file in the folder
     */
    public static void assertFileNumber(FileSystem fs, String folder, int expected) throws IOException {
        FileStatus[] fileStatuses = FileSystemUtil.listSubFiles(fs, folder);
        assertEquals(fileStatuses.length, expected);
    }

    /**
     * Tests that a file on the HDFS cluster contains the given avro.
     *
     * @param path the name of the file on the HDFS cluster
     * @param expected the expected avro record in the file .
     */
    public static void assertReadAvroFile(FileSystem fs, String path, Set<IndexedRecord> expected, boolean part) throws IOException {
        Path p = new Path(path);
        if (fs.isFile(p)) {
            try (DataFileStream<GenericRecord> reader = new DataFileStream<GenericRecord>(
                    new BufferedInputStream(fs.open(new Path(path))), new GenericDatumReader<GenericRecord>())) {
                IndexedRecord record = null;
                while (reader.hasNext()){
                    record = reader.iterator().next();
                    IndexedRecord eqRecord = null;
                    for (IndexedRecord indexedRecord : expected) {
                        if(indexedRecord.equals(record)){
                            eqRecord = indexedRecord;
                            break;
                        }
                    }
                    expected.remove(eqRecord);
                }
            }
            // Check before asserting for the message.
            if (!part && expected.size() != 0)
                assertThat("Not all avro records found: " + expected.iterator().next(), expected, hasSize(0));
        } else if (fs.isDirectory(p)) {
            for (FileStatus fstatus : FileSystemUtil.listSubFiles(fs, p)) {
                assertReadAvroFile(fs, fstatus.getPath().toString(), expected, true);
            }
            // Check before asserting for the message.
            if (expected.size() != 0)
                assertThat("Not all avro records found: " + expected.iterator().next(), expected, hasSize(0));
        } else {
            fail("No such path: " + path);
        }
    }

    /**
     * Tests that a file on the HDFS cluster contains the given parquet.
     *
     * @param path the name of the file on the HDFS cluster
     * @param expected the expected avro record in the file .
     */
    public static void assertReadParquetFile(FileSystem fs, String path, Set<IndexedRecord> expected, boolean part) throws IOException {
        Path p = new Path(path);
        if (fs.isFile(p)) {
            try (AvroParquetReader<GenericRecord> reader = new AvroParquetReader<GenericRecord>(fs.getConf(), new Path(path))) {
                IndexedRecord record = null;
                while (null != (record = reader.read())){
                    IndexedRecord eqRecord = null;
                    for (IndexedRecord indexedRecord : expected) {
                        if(indexedRecord.equals(record)){
                            eqRecord = indexedRecord;
                            break;
                        }
                    }
                    expected.remove(eqRecord);
                }
            }
            // Check before asserting for the message.
            if (!part && expected.size() != 0)
                assertThat("Not all avro records found: " + expected.iterator().next(), expected, hasSize(0));
        } else if (fs.isDirectory(p)) {
            for (FileStatus fstatus : FileSystemUtil.listSubFiles(fs, p)) {
                assertReadParquetFile(fs, fstatus.getPath().toString(), expected, true);
            }
            // Check before asserting for the message.
            if (expected.size() != 0)
                assertThat("Not all avro records found: " + expected.iterator().next(), expected, hasSize(0));
        } else {
            fail("No such path: " + path);
        }
    }

    @Override
    public Statement apply(Statement base, Description d) {
        testName = d.getMethodName();
        return super.apply(base, d);
    }

    @Override
    protected void after() {
        // Only needs to be shut down if it was created.
        if (miniHdfs != null) {
            miniHdfs.shutdown(true);
            miniHdfs = null;
            fs = null;
        }
        super.after();
    }

    /**
     * @return The URL for the name node health page.
     */
    public URL getNameNodeUI() throws IOException {
        getFs();
        return new URL("http://localhost:" + NameNode.getHttpAddress(miniHdfs.getConfiguration(0)).getPort() + "/dfshealth.jsp");
    }

    /**
     * @return The hadoop FileSystem pointing to the simulated cluster.
     */
    public FileSystem getFs() throws IOException {
        // Lazily create the MiniDFSCluster on first use.
        if (miniHdfs == null) {
            System.setProperty(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, newFolder("base").getAbsolutePath());
            System.setProperty(MiniDFSCluster.PROP_TEST_BUILD_DATA, newFolder("build").getAbsolutePath());
            miniHdfs = new MiniDFSCluster.Builder(new Configuration()).numDataNodes(1).format(true).racks(null).build();
            miniHdfs.waitActive();
            fs = miniHdfs.getFileSystem();
        }
        return fs;
    }

    /**
     * @return The hadoop FileSystem pointing to the local disk.
     */
    public FileSystem getLocalFs() throws IOException {
        return FileSystem.getLocal(new Configuration());
    }

    /**
     * @return A new temporary folder on the local filesystem.
     */
    public String getLocalFsNewFolder() throws IOException {
        return getLocalFs().getUri().resolve(new Path(newFolder(testName).toString()).toUri()) + "/";
    }
}
