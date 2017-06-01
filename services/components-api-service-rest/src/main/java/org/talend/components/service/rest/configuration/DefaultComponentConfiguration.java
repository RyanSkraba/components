package org.talend.components.service.rest.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * if default configuration is required then unzip the config artifact in the default config folder.
 */
@Configuration
@ConditionalOnProperty(value = "org.ops4j.pax.url.mvn.repositories")
public class DefaultComponentConfiguration {

    public static final Logger LOG = LoggerFactory.getLogger(DefaultComponentConfiguration.class);

    @Value("${component.default.config.mvn.url:#{null}}")
    private String configtMvnUrlStr;

    @Value("${component.default.config.folder:#{null}}")
    private String configFolderPath;

    /**
     * if default config artifact is specified then unzip it
     */
    @PostConstruct
    public void init() {
        if (configtMvnUrlStr != null && configFolderPath != null) {
            try {
                unzipMvnArtifact(configtMvnUrlStr, configFolderPath);
                LOG.info("unzipped default config [" + configtMvnUrlStr + "] to folder [" + configFolderPath + "]");
            } catch (IOException e) {// only log it
                LOG.error("Failed to extract default configuration artifact [" + configtMvnUrlStr + "] to folder ["
                        + configFolderPath + "]", e);
            }
        } // else do nothing
    }

    private void unzipMvnArtifact(String aConfigtMvnUrlStr, String aConfigFolderPath) throws IOException {
        RuntimeUtil.registerMavenUrlHandler();
        MavenResolver mavenResolver = MavenResolvers.createMavenResolver(null, ServiceConstants.PID);
        File artifactToUnzip = mavenResolver.resolve(aConfigtMvnUrlStr);
        try (ZipFile zipFile = new ZipFile(artifactToUnzip)) {
            try {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    File entryDestination = new File(aConfigFolderPath, entry.getName());
                    if (entry.isDirectory()) {
                        entryDestination.mkdirs();
                    } else {
                        entryDestination.getParentFile().mkdirs();
                        InputStream in = zipFile.getInputStream(entry);
                        OutputStream out = new FileOutputStream(entryDestination);
                        IOUtils.copy(in, out);
                        IOUtils.closeQuietly(in);
                        out.close();
                    }
                }
            } finally {
                zipFile.close();
            }

        }
    }

}
