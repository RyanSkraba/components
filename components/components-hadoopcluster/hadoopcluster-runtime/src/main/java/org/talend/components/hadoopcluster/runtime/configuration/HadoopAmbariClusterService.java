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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.api.model.ApiConfigFile;
import org.apache.hadoop.conf.Configuration;
import org.talend.components.api.exception.ComponentException;

public class HadoopAmbariClusterService implements HadoopClusterService {

    private static final String SUPPORT_FILE = "site"; //$NON-NLS-1$

    private Map<String, Configuration> confs;// only contains *-site.xml

    private List<ApiConfigFile> configFiles;

    public HadoopAmbariClusterService(List<ApiConfigFile> configFiles, List<String> blacklistParams) {
        this.configFiles = configFiles;
        init(blacklistParams);
    }

    private void init(List<String> blacklistParams) {
        confs = new HashMap<>();
        for (ApiConfigFile file : configFiles) {
            String type = file.getType();
            if (!type.endsWith(SUPPORT_FILE)) {
                continue;
            }
            Configuration conf = new Configuration(false);
            Map<String, String> properties = file.getProperties();
            for (String key : properties.keySet()) {
                if (blacklistParams != null && blacklistParams.contains(key)) {
                    continue;
                }
                conf.set(key, properties.get(key));
            }
            confs.put(type, conf);
        }
    }

    @Override
    public Map<String, String> getConfiguration() {
        Map<String, String> confMapping = new HashMap<>();
        for (ApiConfigFile file : configFiles) {
            if (file.getProperties() != null) {
                confMapping.putAll(file.getProperties());
            }
        }
        return confMapping;
    }

    @Override
    public String getConfigurationValue(String key) {
        Map<String, String> confMapping = getConfiguration();
        return confMapping.get(key);
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
        File confFile = new File(folderPath, confName + ".xml"); //$NON-NLS-1$
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
        Set<String> fileNames = new HashSet<>();
        for (ApiConfigFile file : configFiles) {
            fileNames.add(file.getType());
        }
        return fileNames;
    }

    @Override
    public boolean hasConfigurations() {
        return confs.size() > 0;
    }

}
