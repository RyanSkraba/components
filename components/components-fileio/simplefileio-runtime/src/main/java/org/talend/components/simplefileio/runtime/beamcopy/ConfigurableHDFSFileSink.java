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
package org.talend.components.simplefileio.runtime.beamcopy;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import static org.apache.beam.sdk.repackaged.com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.repackaged.com.google.common.collect.Lists;
import org.apache.beam.sdk.repackaged.com.google.common.collect.Maps;
import org.apache.beam.sdk.repackaged.com.google.common.collect.Sets;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.task.JobContextImpl;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.simplefileio.runtime.utils.FileSystemUtil;

/**
 * Copied from https://github.com/apache/beam/commit/89cf4613465647e2711983674879afd5f67c519d
 *
 * This class was modified to add the {@link HDFSWriter#configure(Job)} method, and to use the path when getting the
 * filesystem, and to prevent the filesystem from being cached in the components service.
 *
 * A {@code Sink} for writing records to a Hadoop filesystem using a Hadoop file-based output format.
 *
 * @param <K> The type of keys to be written to the sink.
 * @param <V> The type of values to be written to the sink.
 */
public class ConfigurableHDFSFileSink<K, V> extends Sink<KV<K, V>> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurableHDFSFileSink.class);

    private static final JobID jobId = new JobID(Long.toString(System.currentTimeMillis()),
            new Random().nextInt(Integer.MAX_VALUE));

    protected final String path;

    protected final boolean mergeOutput;

    protected final Class<? extends FileOutputFormat<K, V>> formatClass;

    // workaround to make Configuration serializable
    private final Map<String, String> map;

    public ConfigurableHDFSFileSink(String path, boolean mergeOutput, Class<? extends FileOutputFormat<K, V>> formatClass) {
        this.path = path;
        this.mergeOutput = mergeOutput;
        this.formatClass = formatClass;
        this.map = Maps.newHashMap();
    }

    public ConfigurableHDFSFileSink(String path, boolean mergeOutput, Class<? extends FileOutputFormat<K, V>> formatClass,
            Configuration conf) {
        this(path, mergeOutput, formatClass);
        // serialize conf to map
        for (Map.Entry<String, String> entry : conf) {
            map.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void validate(PipelineOptions options) {
        // The original Beam validate logic was moved to UgiFileSink to permit overwrite.
    }

    @Override
    public Sink.WriteOperation<KV<K, V>, ?> createWriteOperation() {
        return new HDFSWriteOperation<>(this, path, mergeOutput, formatClass);
    }

    protected Job jobInstance() throws IOException {
        Job job = Job.getInstance();
        // deserialize map to conf
        Configuration conf = job.getConfiguration();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            conf.set(entry.getKey(), entry.getValue());
        }
        // TODO: We've explicitly listed all the schemas supported here, but the filesystem schema could be dynamically
        // generated from the path (resolved against the default name node).
        conf.set("fs.gs.impl.disable.cache", "true");
        conf.set("fs.s3t.impl.disable.cache", "true");
        conf.set("fs.file.impl.disable.cache", "true");
        conf.set("fs.hdfs.impl.disable.cache", "true");
        job.setJobID(jobId);
        return job;
    }

    // =======================================================================
    // WriteOperation
    // =======================================================================

    /** {{@link WriteOperation}} for HDFS. */
    public static class HDFSWriteOperation<K, V> extends WriteOperation<KV<K, V>, String> {

        protected final String path;

        protected final boolean mergeOutput;

        protected final Class<? extends FileOutputFormat<K, V>> formatClass;

        private final Sink<KV<K, V>> sink;

        public HDFSWriteOperation(Sink<KV<K, V>> sink, String path, boolean mergeOutput,
                Class<? extends FileOutputFormat<K, V>> formatClass) {
            this.sink = sink;
            this.path = path;
            this.mergeOutput = mergeOutput;
            this.formatClass = formatClass;
        }

        @Override
        public void initialize(PipelineOptions options) throws Exception {
            Job job = ((ConfigurableHDFSFileSink<K, V>) getSink()).jobInstance();
            FileOutputFormat.setOutputPath(job, new Path(path));
        }

        @Override
        public void finalize(Iterable<String> writerResults, PipelineOptions options) throws Exception {
            Job job = ((ConfigurableHDFSFileSink<K, V>) getSink()).jobInstance();
            FileSystem fs = FileSystem.get(new URI(path), job.getConfiguration());

            // Finalize can be called several times. Don't try to recommit if the first part has already succeeded.
            boolean isCommitted = fs.exists(new Path(path, FileOutputCommitter.SUCCEEDED_FILE_NAME));
            if (!isCommitted) {
                // Get expected output shards. Nulls indicate that the task was launched, but didn't
                // process any records.
                Set<String> expected = Sets.newHashSet(writerResults);
                expected.remove(null);

                // If there are 0 output shards, just create output folder.
                if (!expected.iterator().hasNext()) {
                    fs.mkdirs(new Path(path));
                    return;
                }

                // job successful
                JobContext context = new JobContextImpl(job.getConfiguration(), job.getJobID());
                FileOutputCommitter outputCommitter = new FileOutputCommitter(new Path(path), context);
                outputCommitter.commitJob(context);

                // get actual output shards
                Set<String> actual = Sets.newHashSet();
                FileStatus[] statuses = FileSystemUtil.listSubFiles(fs, path);

                checkState(expected.size() == Lists.newArrayList(writerResults).size(),
                        "Data loss due to writer results hash collision");
                for (FileStatus s : statuses) {
                    String name = s.getPath().getName();
                    int pos = name.indexOf('.');
                    actual.add(pos > 0 ? name.substring(0, pos) : name);
                }

                checkState(actual.equals(expected), "Writer results and output files do not match");

                // rename output shards to Hadoop style, i.e. part-r-00000.txt
                int i = 0;
                for (FileStatus s : statuses) {
                    String name = s.getPath().getName();
                    int pos = name.indexOf('.');
                    String ext = pos > 0 ? name.substring(pos) : "";
                    rename(fs, s.getPath(), String.format("part-r-%05d%s", i, ext));
                    i++;
                }
            }

            FileStatus[] sourceStatuses = FileSystemUtil.listSubFiles(fs, path); // after rename, before generate merged file
            if (sourceStatuses.length > 0 && mergeOutput) {
                String sourceFileName = sourceStatuses[0].getPath().getName();
                String extension =
                        sourceFileName.indexOf('.') > 0 ? sourceFileName.substring(sourceFileName.indexOf('.')) : "";
                String finalPath = path + String.format("/part-r-merged%s", extension);
                fs.delete(new Path(finalPath), true); // finalize method may be called multiple times, be sure idempotent
                LOG.info("Start to merge files in {} to {}", path, finalPath);
                mergeOutput(fs, path, finalPath);
                LOG.info("Merge files in {} to {} successful, start to delete the source files.", path, finalPath);
                for (FileStatus sourceStatus : sourceStatuses) {
                    fs.delete(sourceStatus.getPath(), true);
                }
            }

        }

        private void rename(FileSystem fs, Path sourcePath, String newFileName) throws Exception {
            fs.rename(sourcePath, new Path(sourcePath.getParent(), newFileName));
        }

        protected void mergeOutput(FileSystem fs, String sourceFolder, String targetFile) throws IOException {
            // need to be implement
            throw new IOException("Merge must be implemented in a subclass.");
        }

        @Override
        public Writer<KV<K, V>, String> createWriter(PipelineOptions options) throws Exception {
            return new HDFSWriter<>(this, path, formatClass);
        }

        @Override
        public Sink<KV<K, V>> getSink() {
            return sink;
        }

        @Override
        public Coder<String> getWriterResultCoder() {
            return StringUtf8Coder.of();
        }

    }

    // =======================================================================
    // Writer
    // =======================================================================

    /** {{@link Writer}} for HDFS files. */
    public static class HDFSWriter<K, V> extends Writer<KV<K, V>, String> {

        private final HDFSWriteOperation<K, V> writeOperation;

        private final String path;

        private final Class<? extends FileOutputFormat<K, V>> formatClass;

        // unique hash for each task
        private int hash;

        private TaskAttemptContext context;

        private RecordWriter<K, V> recordWriter;

        private FileOutputCommitter outputCommitter;

        public HDFSWriter(HDFSWriteOperation<K, V> writeOperation, String path,
                Class<? extends FileOutputFormat<K, V>> formatClass) {
            this.writeOperation = writeOperation;
            this.path = path;
            this.formatClass = formatClass;
        }

        protected void configure(Job job) {
        }

        @Override
        public void open(String uId) throws Exception {
            this.hash = uId.hashCode();

            Job job = ((ConfigurableHDFSFileSink<K, V>) getWriteOperation().getSink()).jobInstance();
            FileOutputFormat.setOutputPath(job, new Path(path));

            // Each Writer is responsible for writing one bundle of elements and is represented by one
            // unique Hadoop task based on uId/hash. All tasks share the same job ID. Since Dataflow
            // handles retrying of failed bundles, each task has one attempt only.
            JobID jobId = job.getJobID();
            TaskID taskId = new TaskID(jobId, TaskType.REDUCE, hash);
            configure(job);
            context = new TaskAttemptContextImpl(job.getConfiguration(), new TaskAttemptID(taskId, 0));

            FileOutputFormat<K, V> outputFormat = formatClass.newInstance();
            recordWriter = outputFormat.getRecordWriter(context);
            outputCommitter = (FileOutputCommitter) outputFormat.getOutputCommitter(context);
        }

        @Override
        public void write(KV<K, V> value) throws Exception {
            recordWriter.write(value.getKey(), value.getValue());
        }

        @Override
        public String close() throws Exception {
            // task/attempt successful
            recordWriter.close(context);
            outputCommitter.commitTask(context);

            // result is prefix of the output file name
            return String.format("part-r-%d", hash);
        }

        @Override
        public WriteOperation<KV<K, V>, String> getWriteOperation() {
            return writeOperation;
        }

        /**
         * Perform a manual commit of the result file. This should be avoided in almost all
         * normal operations, but is necessary for TFD-3404 for now.
         */
        public void commitManually(String committed, String rewriteFile) throws IOException {
            Path srcDir = outputCommitter.getCommittedTaskPath(context);
            Path src = new Path(srcDir, committed);
            Path dst = new Path(path, rewriteFile);

            FileSystem fs = src.getFileSystem(context.getConfiguration());

            if (fs.exists(dst) && !fs.delete(dst, true)) {
                throw new IOException("Could not delete " + dst);
            }

            if (!fs.exists(src)) {
                // check if the file exists with a glob (sometimes extensions are added).
                FileStatus[] matches = fs.globStatus(new Path(src.getParent(), committed + "*"));
                if (matches != null && matches.length == 1)
                    src = matches[0].getPath();
                else
                    throw new IOException("Could not find committed file " + src);
            }

            if (!fs.rename(src, dst)) {
                throw new IOException("Could not rename " + src + " to " + dst);
            }

            fs.delete(srcDir, true);
        }
    }

}
