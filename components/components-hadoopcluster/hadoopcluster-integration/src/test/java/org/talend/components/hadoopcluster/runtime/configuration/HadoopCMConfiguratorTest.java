package org.talend.components.hadoopcluster.runtime.configuration;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.talend.components.hadoopcluster.runtime.configuration.HadoopClusterTestConstants.CDH58_MANAGER_PASS;
import static org.talend.components.hadoopcluster.runtime.configuration.HadoopClusterTestConstants.CDH58_MANAGER_URL;
import static org.talend.components.hadoopcluster.runtime.configuration.HadoopClusterTestConstants.CDH58_MANAGER_USER;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.hadoopcluster.configuration.input.HadoopClusterConfiguration;
import org.talend.components.hadoopcluster.configuration.input.HadoopClusterConfigurationInputProperties;
import org.talend.components.hadoopcluster.runtime.configuration.input.ClouderaManagerConfigurationSource;

public class HadoopCMConfiguratorTest {

    public static void checkCluster(HadoopConfigurator configurator, String... clusterNames) throws Exception {
        String[] allClusters = configurator.getAllClusters().toArray(new String[configurator.getAllClusters().size()]);
        Arrays.sort(allClusters);
        Arrays.sort(clusterNames);
        assertArrayEquals(clusterNames, allClusters);
    }

    public static void checkService(Map<HadoopHostedService, HadoopClusterService> services,
            HadoopHostedService... expectedServices) throws Exception {
        assertEquals(expectedServices.length, services.size());
        Arrays.sort(expectedServices);
        HadoopHostedService[] array = services.keySet().toArray(new HadoopHostedService[services.keySet().size()]);
        Arrays.sort(array);
        assertArrayEquals(expectedServices, array);
    }

    public static void checkServiceConf(HadoopClusterService service, String... confFileNames) throws Exception {
        assertEquals(true, service.hasConfigurations());
        assertEquals(confFileNames.length, service.getConfFiles().size());
        String[] hdfsConfs = service.getConfFiles().toArray(new String[service.getConfFiles().size()]);
        Arrays.sort(confFileNames);
        Arrays.sort(hdfsConfs);
        assertArrayEquals(confFileNames, hdfsConfs);
    }

    @Test
    @Ignore("after cm58 parameters available")
    public void testCDH58() throws Exception {
        String folder = "/tmp/cm";
        HadoopConfigurator configurator = new HadoopCMConfigurator.Builder().withUrl(new URL(CDH58_MANAGER_URL))
                .withUsernamePassword(CDH58_MANAGER_USER, CDH58_MANAGER_PASS).build();

        checkCluster(configurator, "Cluster 1");

        HadoopCluster cluster = configurator.getCluster(configurator.getAllClusters().get(0));
        Map<HadoopHostedService, HadoopClusterService> services = cluster.getHostedServices();

        checkService(services, HadoopHostedService.HDFS, HadoopHostedService.YARN, HadoopHostedService.HIVE,
                HadoopHostedService.HBASE);

        checkServiceConf(services.get(HadoopHostedService.HDFS), "hdfs-site.xml", "core-site.xml");
        checkServiceConf(services.get(HadoopHostedService.YARN), "yarn-site.xml", "hdfs-site.xml", "core-site.xml",
                "mapred-site.xml");
        checkServiceConf(services.get(HadoopHostedService.HIVE), "hive-site.xml", "yarn-site.xml", "hdfs-site.xml",
                "core-site.xml", "mapred-site.xml");
        checkServiceConf(services.get(HadoopHostedService.HBASE), "hbase-site.xml", "hdfs-site.xml", "core-site.xml");

        // test blacklist
        String key = "fs.defaultFS";
        assertEquals("hdfs://tldns01", services.get(HadoopHostedService.HDFS).getConfigurationValue(key));
        cluster.setBlacklistParams(Arrays.asList(new String[] { key }));
        assertNull(cluster.getHostedServices().get(HadoopHostedService.HDFS).getConfigurationValue(key));

    }

    @Test
    @Ignore("after cm58 parameters available")
    public void testCDH58_ComponentProperties() throws IOException {
        HadoopClusterConfigurationInputProperties properties = new HadoopClusterConfigurationInputProperties("properties");
        properties.init();
        properties.url.setValue(CDH58_MANAGER_URL);
        properties.basicAuth.useAuth.setValue(true);
        properties.basicAuth.userId.setValue(CDH58_MANAGER_USER);
        properties.basicAuth.password.setValue(CDH58_MANAGER_PASS);

        List<String> results = new ArrayList<>();
        ClouderaManagerConfigurationSource source = new ClouderaManagerConfigurationSource();
        source.initialize(null, properties);
        BoundedReader reader = source.createReader(null);
        for (boolean available = reader.start(); available; available = reader.advance()) {
            HadoopClusterConfiguration current = (HadoopClusterConfiguration) reader.getCurrent();
            results.add(current.get(0).toString() + current.get(1) + current.get(2));
        }

        assertEquals(14, results.size());
        assertThat(results, hasItem("Cluster 1HIVEyarn-site.xml"));
        assertThat(results, hasItem("Cluster 1HIVEhdfs-site.xml"));
        assertThat(results, hasItem("Cluster 1HIVEcore-site.xml"));
        assertThat(results, hasItem("Cluster 1HIVEmapred-site.xml"));
        assertThat(results, hasItem("Cluster 1HIVEhive-site.xml"));
        assertThat(results, hasItem("Cluster 1HBASEhdfs-site.xml"));
        assertThat(results, hasItem("Cluster 1HBASEcore-site.xml"));
        assertThat(results, hasItem("Cluster 1HBASEhbase-site.xml"));
        assertThat(results, hasItem("Cluster 1YARNyarn-site.xml"));
        assertThat(results, hasItem("Cluster 1YARNhdfs-site.xml"));
        assertThat(results, hasItem("Cluster 1YARNcore-site.xml"));
        assertThat(results, hasItem("Cluster 1YARNmapred-site.xml"));
        assertThat(results, hasItem("Cluster 1HDFShdfs-site.xml"));
        assertThat(results, hasItem("Cluster 1HDFScore-site.xml"));
    }
}
