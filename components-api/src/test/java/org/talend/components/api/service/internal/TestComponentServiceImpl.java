// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.service.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * created by sgandon on 13 oct. 2015
 */
public class TestComponentServiceImpl {

    List<String> NO_PROFILE_DEPS = Arrays.asList("mvn:org.springframework/spring-webmvc/1.0/jar", //$NON-NLS-1$
            "mvn:org.eclipse.aether/aether-api/1.0.0.v20140518/jar", "mvn:foo/bar/1.6.3/jar", "mvn:junit/junit/4.11/jar",
            "mvn:org.talend.components.salesforce/partner/34.0.0/jar");

    /**
     * Test method for
     * {@link org.talend.components.api.service.internal.ComponentServiceImpl#loadPom(java.io.InputStream, org.talend.components.api.service.internal.MavenBooter)}
     * .
     */
    @Test
    public void testLoadPom() throws ModelBuildingException, URISyntaxException, IOException {
        ComponentServiceImpl componentServiceImpl = new ComponentServiceImpl(null);
        URL pomUrl = this.getClass().getResource("pom.xml"); //$NON-NLS-1$
        final File temporaryFolder = new File(new File(pomUrl.toURI()).getParentFile(), "tempFolder");
        try {
            Model pom = componentServiceImpl.loadPom(this.getClass().getResourceAsStream("pom.xml"), new MavenBooter() {

                @Override
                File getDefaultLocalRepoDir() {
                    return temporaryFolder;
                }

            }, Collections.EMPTY_LIST);
            List<Dependency> dependencies = pom.getDependencies();
            checkDependencies(dependencies, NO_PROFILE_DEPS.toArray(new String[NO_PROFILE_DEPS.size()]));
        } finally {
            FileUtils.deleteDirectory(temporaryFolder);
        }
    }

    @Test
    @Ignore("This requires authentication credential in the settings.xml")
    public void testLoadPomWithAuthentification() throws ModelBuildingException, URISyntaxException, IOException {
        ComponentServiceImpl componentServiceImpl = new ComponentServiceImpl(null);
        URL pomUrl = this.getClass().getResource("pom.xml"); //$NON-NLS-1$
        final File temporaryFolder = new File(new File(pomUrl.toURI()).getParentFile(), "tempFolder");
        System.setProperty(MavenBooter.TALEND_MAVEN_REMOTE_REPOSITORY_ID_SYS_PROP, "releases");
        System.setProperty(MavenBooter.TALEND_MAVEN_REMOTE_REPOSITORY_URL_SYS_PROP,
                "http://newbuild.talend.com:8081/nexus/content/repositories/releases/");

        try {
            Model pom = componentServiceImpl.loadPom(this.getClass().getResourceAsStream("pom_with_authentified_deps.xml"),
                    new MavenBooter() {

                        @Override
                        File getDefaultLocalRepoDir() {
                            return temporaryFolder;
                        }

                    }, Collections.EMPTY_LIST);
            List<Dependency> dependencies = pom.getDependencies();
            checkDependencies(dependencies, NO_PROFILE_DEPS.toArray(new String[NO_PROFILE_DEPS.size()]));
        } finally {
            FileUtils.deleteDirectory(temporaryFolder);
        }
    }

    @Test
    public void testLoadPomWithProfiles() throws ModelBuildingException {
        ComponentServiceImpl componentServiceImpl = new ComponentServiceImpl(null);
        Model pom = componentServiceImpl.loadPom(this.getClass().getResourceAsStream("pom.xml"), new MavenBooter(), //$NON-NLS-1$
                Arrays.asList("prof1", "prof2"));
        List<Dependency> dependencies = pom.getDependencies();
        List<String> expected = new ArrayList<>(NO_PROFILE_DEPS);
        expected.addAll(Arrays.asList("mvn:org.eclipse.aether/aether-util/1.0.0.v20140518/jar",
                "mvn:org.eclipse.aether/aether-bar/1.0.0.v20140518/jar"));
        checkDependencies(dependencies, expected.toArray(new String[expected.size()]));
    }

    /**
     * DOC sgandon Comment method "checkDependencies".
     * 
     * @param dependencies
     * @param string
     */
    private void checkDependencies(List<Dependency> dependencies, String... mvnStrings) {
        List<String> mvnStringList = Arrays.asList(mvnStrings);
        for (Dependency dep : dependencies) {
            String mvnStr = "mvn:" + dep.getGroupId() + "/" + dep.getArtifactId() + "/" + dep.getVersion()
                    + (dep.getType() == null ? ""
                            : "/" + dep.getType() + (dep.getClassifier() == null ? "" : "/" + dep.getClassifier()));
            if (!mvnStringList.contains(mvnStr)) {
                fail("dependency [" + dep + "] was not found in expected dependencies.");
            }
        }

    }

}
