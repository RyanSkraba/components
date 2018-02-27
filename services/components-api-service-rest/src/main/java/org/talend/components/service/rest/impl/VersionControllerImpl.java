// ==============================================================================
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
// ==============================================================================

package org.talend.components.service.rest.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.slf4j.Logger;
import org.talend.components.service.rest.VersionController;
import org.talend.components.service.rest.dto.VersionDto;
import org.talend.daikon.annotation.ServiceImplementation;

@ServiceImplementation
public class VersionControllerImpl implements VersionController {

    /** A properties file generated at build time using the Maven git-commit-id-plugin. */
    public final static String PROPERTIES = "git.properties";

    private static final Logger log = getLogger(VersionControllerImpl.class);

    /** Singleton instance of the version to return. */
    private VersionDto instance = null;

    @Override
    public VersionDto getVersion() {
        if (instance == null) {
            // Fetch the version information from the git.properties file that was generated at build time.
            try {
                java.util.Properties gitProperties = new java.util.Properties();
                gitProperties.load(getClass().getClassLoader().getResourceAsStream(PROPERTIES));
                instance = new VersionDto( //
                        gitProperties.getProperty("git.build.version"), //
                        gitProperties.getProperty("git.commit.id"), //
                        gitProperties.getProperty("git.build.time"));
            } catch (IOException e) {
                log.warn("Cannot fetch version information", e);
                instance = new VersionDto();
            }

        }
        return instance;
    }
}
