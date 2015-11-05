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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * created by sgandon on 13 oct. 2015
 */
public class ComponentServiceImplTestIT {

    String DIRECT_DEPS = "mvn:org.springframework/spring-webmvc/1.0/jar,mvn:org.eclipse.aether/aether-api/1.0.0.v20140518/jar,"
            + "mvn:foo/bar/1.6.3/jar,mvn:org.talend.components.salesforce/partner/34.0.0/jar";

    String FULL_DEPS = DIRECT_DEPS + ",mvn:org.talend.components/org.talend.test.dependencies/0.1-SNAPSHOT//bundle";

    String DEPS_PROF12 = DIRECT_DEPS
            + ",mvn:org.eclipse.aether/aether-util/1.0.0.v20140518/jar,mvn:org.eclipse.aether/aether-bar/1.0.0.v20140518/jar";

    String FULL_DEPS_TEST = FULL_DEPS
            + ",mvn:junit/junit/4.11/jar,mvn:org.hamcrest/hamcrest-library/1.3/jar,mvn:org.hamcrest/hamcrest-core/1.3/jar";

    // ,mvn:junit/junit/4.11/jar
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
            checkDependencies(dependencies, "runtime", DIRECT_DEPS.split(",")); //$NON-NLS-1$//$NON-NLS-2$
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
            checkDependencies(dependencies, "runtime", DIRECT_DEPS.split(","));
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
        checkDependencies(dependencies, "runtime", DEPS_PROF12.split(","));
    }

    /**
     * DOC sgandon Comment method "checkDependencies".
     * 
     * @param dependencies
     * @param scope
     * @param string
     */
    private void checkDependencies(List<Dependency> dependencies, String scope, String... mvnStrings) {
        List<String> mvnStringList = Arrays.asList(mvnStrings);
        for (Dependency dep : dependencies) {
            String mvnStr = "mvn:" + dep.getGroupId() + "/" + dep.getArtifactId() + "/" + dep.getVersion()
                    + (dep.getType() == null ? ""
                            : "/" + dep.getType() + (dep.getClassifier() == null ? "" : "/" + dep.getClassifier()));
            if (!mvnStringList.contains(mvnStr) && scope.equals(dep.getScope())) {
                fail("dependency [" + dep + "] was not found in expected dependencies.");
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRuntimeDependencies() throws ModelBuildingException, URISyntaxException, IOException,
            DependencyCollectionException, DependencyResolutionException, XmlPullParserException {
        ComponentServiceImpl componentServiceImpl = new ComponentServiceImpl(null);
        URL pomUrl = this.getClass().getResource("pom.xml"); //$NON-NLS-1$
        InputStream stream = pomUrl.openStream();
        try {
            Set<String> mavenUriDependencies = componentServiceImpl.computeDependenciesFromPom(stream, "test", "provided");
            assertThat(mavenUriDependencies, Matchers.containsInAnyOrder(FULL_DEPS.split(","))); //$NON-NLS-1$
        } finally {
            stream.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetTestDependenciesIncludingTransitive() throws ModelBuildingException, URISyntaxException, IOException,
            DependencyCollectionException, DependencyResolutionException, XmlPullParserException {
        ComponentServiceImpl componentServiceImpl = new ComponentServiceImpl(null);
        URL pomUrl = this.getClass().getResource("pom.xml"); //$NON-NLS-1$
        InputStream stream = pomUrl.openStream();
        try {
            Set<String> mavenUriDependencies = componentServiceImpl.computeDependenciesFromPom(stream, "provided");
            assertThat(mavenUriDependencies, Matchers.containsInAnyOrder(FULL_DEPS_TEST.split(","))); //$NON-NLS-1$
        } finally {
            stream.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetTestDependenciesCache() throws ModelBuildingException, URISyntaxException, IOException,
            DependencyCollectionException, DependencyResolutionException, XmlPullParserException {
        ComponentServiceImpl componentServiceImpl = new ComponentServiceImpl(null);
        URL pomUrl = this.getClass().getResource("pom.xml"); //$NON-NLS-1$
        InputStream stream = pomUrl.openStream();
        Set<String> mavenUriDependencies;
        try {
            mavenUriDependencies = componentServiceImpl.computeDependenciesFromPom(stream, "provided");
            assertThat(mavenUriDependencies, Matchers.containsInAnyOrder(FULL_DEPS_TEST.split(","))); //$NON-NLS-1$
        } finally {
            stream.close();
        }
        stream = pomUrl.openStream();
        try {
            Set<String> mavenUriDependencies2 = componentServiceImpl.computeDependenciesFromPom(stream, "provided");
            assertThat(mavenUriDependencies, Matchers.equalTo(mavenUriDependencies2));
        } finally {
            stream.close();
        }
    }

}
