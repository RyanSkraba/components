// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.HashMap;
import java.util.Map;

import org.apache.beam.runners.spark.SparkPipelineOptions;
import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;

import avro.shaded.com.google.common.collect.ImmutableMap;

/**
 * Reusable for creating a {@link org.apache.spark.api.java.JavaSparkContext} pointing to a specific cluster for Spark
 * integration tests.
 */
public class SparkIntegrationTestResource extends TemporaryFolder {

    /** Default namenode. */
    private final static String NN_DEFAULT = "hdfs://talend-cdh580.weave.local:8020";

    /** Default resource manager. */
    private final static String RM_DEFAULT = "talend-cdh580.weave.local:8032";

    /** Default resource manager scheduler. */
    private final static String RMS_DEFAULT = "talend-cdh580.weave.local:8030";

    /** Default spark master. */
    private final static String SM_DEFAULT = "yarn-client";

    /** Default hadoop configuration. */
    public final static Map<String, String> HADOOP_CONF_DEFAULT = ImmutableMap
            .<String, String> builder()
            .put(CommonConfigurationKeys.FS_DEFAULT_NAME_KEY, NN_DEFAULT)
            .put(YarnConfiguration.RM_ADDRESS, RM_DEFAULT)
            .put(YarnConfiguration.RM_SCHEDULER_ADDRESS, RMS_DEFAULT)
            .put(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                    "/etc/hadoop/conf, /usr/lib/hadoop/*, /usr/lib/hadoop/lib/*, "
                            + "/usr/lib/hadoop-hdfs/*, /usr/lib/hadoop-hdfs/lib/*, "
                            + "/usr/lib/hadoop-mapreduce/*, /usr/lib/hadoop-mapreduce/lib/*, "
                            + "/usr/lib/hadoop-yarn/*, /usr/lib/hadoop-yarn/lib/*, " + "/usr/lib/spark/lib/*") //
            .build();

    /** name node */
    private final String nn;

    /** resource manager */
    private final String rm;

    /** resource manager scheduler */
    private final String rms;

    /** spark master */
    private final String sm;

    /** any hadoop configuration */
    private final Map<String, String> hadoopConf;

    /** The current pipeline options for the test. */
    protected SparkPipelineOptions options = null;

    /**
     * Use the static constructors to create an instance of this resource.
     */
    private SparkIntegrationTestResource(String nn, String rm, String rms, String sm, Map<String, String> hadoopConf) {
        this.nn = nn;
        this.rm = rm;
        this.rms = rms;
        this.sm = sm;
        this.hadoopConf = hadoopConf;
    }

    public static SparkIntegrationTestResource ofYarnClient(String nn, String rm, String rms, Map<String, String> hadoopConf) {
        return new SparkIntegrationTestResource(nn, rm, rms, "yarn-client", hadoopConf);
    }

    public static SparkIntegrationTestResource ofDefault() {
        return new SparkIntegrationTestResource(NN_DEFAULT, RM_DEFAULT, RMS_DEFAULT, SM_DEFAULT, HADOOP_CONF_DEFAULT);
    }

    public static SparkIntegrationTestResource ofLocal() {
        return new SparkIntegrationTestResource("file:///tmp/localfs", "local", "local", "local[2]",
                new HashMap<String, String>());
    }

    /**
     * @return the options used to create this pipeline. These can be or changed before the Pipeline is created.
     */
    public SparkPipelineOptions getOptions() {
        if (options == null) {
            options = PipelineOptionsFactory.as(SparkPipelineOptions.class);
            options.setRunner(SparkRunner.class);
        }
        return options;
    }

    /**
     * @return a clean hadoop configuration created from the options in this resource.
     */
    public Configuration createHadoopConfiguration() {
        Configuration conf = new Configuration();
        for (Map.Entry<String, String> kv : hadoopConf.entrySet())
            conf.set(kv.getKey(), kv.getValue());
        return conf;
    }

    /**
     * @return a clean spark configuration created from the options in this resource.
     */
    public SparkConf createSparkConf(String appName) {
        SparkConf conf = new SparkConf();
        conf.setAppName(appName);
        conf.setMaster(sm);
        // conf.set("spark.driver.host", "10.42.30.148");
        for (Map.Entry<String, String> kv : hadoopConf.entrySet())
            conf.set("spark.hadoop." + kv.getKey(), kv.getValue());
        return conf;
    }

    /**
     * @return a new pipeline created from the current state of {@link #getOptions()}.
     */
    public Pipeline createPipeline() {
        Pipeline p = Pipeline.create(getOptions());
        LazyAvroCoder.registerAsFallback(p);
        return p;
    }

    /**
     * Configure the options to the defaults and start the spark context.
     */
    @Override
    public Statement apply(Statement base, Description d) {
        getOptions().setUsesProvidedSparkContext(true);
        getOptions().setProvidedSparkContext(new JavaSparkContext(createSparkConf(d.getMethodName())));
        return super.apply(base, d);
    }

    /**
     * Clean up the Spark context after the test is run.
     */
    @Override
    protected void after() {
        if (options != null) {
            JavaSparkContext jsc = options.getProvidedSparkContext();
            if (jsc != null)
                jsc.close();
            options = null;
        }
    }
}