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
package org.talend.components.hadoopcluster.runtime.configuration;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.talend.components.api.exception.ComponentException;

import com.cloudera.api.swagger.ServicesResourceApi;
import com.cloudera.api.swagger.client.ApiException;

public class HadoopCMClusterService implements HadoopClusterService {

    private static final String IGNORE_ERROR_MSG = "does not require a client configuration";
    private static final String SUPPORT_FILE = "site.xml";

    private ServicesResourceApi serviceAPI;

    private String serviceName;

    private String clusterName;

    private Map<String, Configuration> confs;// only contains *-site.xml

    public HadoopCMClusterService(String clusterName, String serviceName, ServicesResourceApi serviceAPI,
            List<String> blacklistParams) {
        this.serviceName = serviceName;
        this.clusterName = clusterName;
        this.serviceAPI = serviceAPI;
        init(blacklistParams);
    }

    private String getConfFileName(String originalName) {
        if (originalName.contains("/")) {
            return originalName.substring(originalName.lastIndexOf("/") + 1, originalName.length());
        }
        return originalName;
    }

    private Configuration filterByBlacklist(Configuration originalConf, List<String> blacklist) {
        if (blacklist != null && blacklist.size() > 0) {
            Configuration filteredConf = new Configuration(false);
            Iterator<Entry<String, String>> iterator = originalConf.iterator();
            while (iterator.hasNext()) {
                Entry<String, String> next = iterator.next();
                if (blacklist.contains(next.getKey())) {
                    continue;
                }
                filteredConf.set(next.getKey(), next.getValue());
            }
            originalConf = filteredConf;
        }
        return originalConf;
    }

    private void writeZipIntoFile(ZipInputStream zipInputStream, File file) throws IOException {
        BufferedWriter configOutput = null;
        try {
            int read;
            configOutput = new BufferedWriter(new FileWriter(file));
            while (zipInputStream.available() > 0) {
                if ((read = zipInputStream.read()) != -1) {
                    configOutput.write(read);
                }
            }
        } finally {
            if (configOutput != null) {
                configOutput.close();
            }
        }
    }

    private void init(List<String> blacklistParams) {
        confs = new HashMap<>();

        try {
            File configZipFile = this.serviceAPI.getClientConfig(this.clusterName, this.serviceName);
            File directory = new File(System.getProperty("java.io.tmpdir"), "Talend_Hadoop_Wizard_" + serviceName
                    + String.valueOf(new Date().getTime()) + Thread.currentThread().getId());
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(configZipFile));
            ZipEntry configInputZipEntry = null;
            while ((configInputZipEntry = zipInputStream.getNextEntry()) != null) {
                String configFile = getConfFileName(configInputZipEntry.getName());
                if (!configFile.endsWith(SUPPORT_FILE)) {
                    continue;
                }
                directory.mkdirs();
                File file = new File(directory, configFile);

                writeZipIntoFile(zipInputStream, file);

                Configuration conf = new Configuration(false);
                conf.addResource(new Path(file.toURI())); // build configuration by file
                conf = filterByBlacklist(conf, blacklistParams);
                confs.put(configFile, conf);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ApiException e) {
            if (e.getCode() == 400 && e.getResponseBody().contains(IGNORE_ERROR_MSG)) {
                java.util.logging.Logger.getLogger(this.getClass().getCanonicalName()).log(Level.INFO,
                        "service: " + this.serviceName + " " + IGNORE_ERROR_MSG);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Map<String, String> getConfiguration() {
        Map<String, String> confMapping = new HashMap<>();
        for (String key : confs.keySet()) {
            confMapping.putAll(getConfiguration(key));
        }
        return confMapping;
    }

    @Override
    public String getConfigurationValue(String key) {
        Map<String, String> confMapping = getConfiguration();
        return confMapping.get(key);
    }

    private Map<String, String> getConfiguration(String confName) {
        Configuration conf = confs.get(confName);
        return conf.getValByRegex(".*"); //$NON-NLS-1$ ;
    }

    @Override
    public void exportConfigurationToXml(String folderPath) {
        for (String key : confs.keySet()) {
            exportConfigurationToXml(folderPath, key);
        }
    }

    @Override
    public String getConfFileContent(String confFileName) {
        Configuration conf = confs.get(confFileName);
        if (conf == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            conf.writeXml(baos);
        } catch (IOException e) {
            throw new ComponentException(e);
        }
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ComponentException(e);
        }
    }

    private void exportConfigurationToXml(String folderPath, String confName) {
        Configuration conf = confs.get(confName);
        if (conf == null) {
            return;
        }
        File confFile = new File(folderPath, confName);
        confFile.getParentFile().mkdirs();
        OutputStream os;
        try {
            os = new FileOutputStream(confFile.getAbsolutePath());
            conf.writeXml(os);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getConfFiles() {
        return confs.keySet();
    }

    @Override
    public boolean hasConfigurations() {
        return confs.size() > 0;
    }

}
