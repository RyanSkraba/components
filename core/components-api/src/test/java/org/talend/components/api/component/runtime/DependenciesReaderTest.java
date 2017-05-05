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
package org.talend.components.api.component.runtime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.runtime.RuntimeUtil;

import shaded.org.apache.commons.io.IOUtils;

public class DependenciesReaderTest {

    private static final Matcher<Iterable<? extends String>> CONTAINS_IN_ANY_ORDER_DEPS = containsInAnyOrder(
            "mvn:org.apache.maven/maven-core/3.3.3/jar", //
            "mvn:org.eclipse.sisu/org.eclipse.sisu.plexus/0.0.0.M2a/jar", //
            "mvn:org.apache.maven/maven-artifact/3.3.3/jar", //
            "mvn:org.eclipse.aether/aether-transport-file/1.0.0.v20140518/jar", //
            "mvn:org.talend.components/file-input/0.1.0.SNAPSHOT/jar"//
    );

    private static final Logger LOG = LoggerFactory.getLogger(DependenciesReaderTest.class);

    private static final String TEST_GROUPID = "org.talend.components.api.test";

    private static final String TEST_ARTEFACTID = "test-components";

    private static final String TEST_VERSION = "1.0";

    private static String tempMavenFilePath;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void init() throws IOException {
        RuntimeUtil.registerMavenUrlHandler();
        createAndDeployArtifact();
    }

    @AfterClass
    public static void removeTmpFiles() throws IOException {
        if (tempMavenFilePath != null) {
            new File(tempMavenFilePath).delete();
        }
    }

    private static void createAndDeployArtifact() throws IOException {
        // create jar file
        File tempFile = File.createTempFile("comps-api-tests", ".jar");
        try {
            // adds a dep.txt entry
            LOG.debug("created temp artifact jar" + tempFile.getAbsolutePath());
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
            String depsEntryPath = "META-INF/maven/" + TEST_GROUPID + "/" + TEST_ARTEFACTID + "/dependencies.txt";
            ZipEntry e = new ZipEntry(depsEntryPath);
            out.putNextEntry(e);

            InputStream depTxtStream = DependenciesReaderTest.class.getResourceAsStream("/" + depsEntryPath);
            byte[] data = IOUtils.toByteArray(depTxtStream);
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
            // deploy it
            MavenResolver mavenResolver = MavenResolvers.createMavenResolver(null, "foo");
            mavenResolver.upload(TEST_GROUPID, TEST_ARTEFACTID, "jar", "jar", TEST_VERSION, tempFile);
            tempMavenFilePath = mavenResolver.resolve(TEST_GROUPID, TEST_ARTEFACTID, "jar", "jar", TEST_VERSION)
                    .getAbsolutePath();
            LOG.debug("artifact deployed:" + tempMavenFilePath);

        } finally {
            // remove it
            tempFile.delete();
        }
    }

    /**
     * Test method for parseMvnUri
     * {@link org.talend.components.api.service.common.ComponentServiceImpl#parseMvnUri(java.lang.String)} .
     */
    @Test
    public void testparseMvnUri() {
        DependenciesReader depReader = new DependenciesReader(null, null);
        String parsedMvnUri = depReader
                .parseMvnUri("     org.talend.components:components-api:test-jar:tests:0.4.0.BUILD-SNAPSHOT:test");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/test-jar/tests", parsedMvnUri);
        parsedMvnUri = depReader.parseMvnUri("    org.talend.components:components-api:jar:0.4.0.BUILD-SNAPSHOT:compile   ");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/jar", parsedMvnUri);
    }

    @Test
    public void testParseDependencies() throws IOException {
        DependenciesReader dependenciesReader = new DependenciesReader(null, null);
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream("dep.txt")) {
            Set<String> deps = dependenciesReader.parseDependencies(resourceAsStream);
            assertEquals(5, deps.size());
            assertThat(deps, CONTAINS_IN_ANY_ORDER_DEPS);
        }
    }

    @Test
    public void testComputeDesignDependenciesPath() {
        DependenciesReader dependenciesReader = new DependenciesReader("foo", "bar");
        assertEquals("META-INF/maven/foo/bar/dependencies.txt", dependenciesReader.getDependencyFilePath());

    }

    @Test
    public void testGetDependencies() throws IOException {
        DependenciesReader dependenciesReader = new DependenciesReader("org.talend.components.api.test", "test-components");
        Set<String> deps = dependenciesReader.getDependencies(this.getClass().getClassLoader());
        assertEquals(5, deps.size());
        assertThat(deps, CONTAINS_IN_ANY_ORDER_DEPS);
    }

    @Test
    public void testComputePathToDepsFromMvnUrl() throws MalformedURLException {
        String computePathToDepsFromMvnUrl = DependenciesReader
                .computeDependenciesFilePath(new URL("mvn:org.talend.components/file-input/0.1.0.SNAPSHOT/jar"));
        assertThat("META-INF/maven/org.talend.components/file-input/dependencies.txt", equalTo(computePathToDepsFromMvnUrl));

        thrown.expect(IllegalArgumentException.class);
        computePathToDepsFromMvnUrl = DependenciesReader
                .computeDependenciesFilePath(new URL("file:org.talend.components/file-input/0.1.0.SNAPSHOT/jar"));
    }

    @Test
    public void testextractDepenenciesFromJarMvnUrl() throws MalformedURLException {
        List<URL> deps = DependenciesReader
                .extractDependencies(new URL("mvn:" + TEST_GROUPID + "/" + TEST_ARTEFACTID + "/" + TEST_VERSION + "/jar/jar"));
        List<String> urlStrs = new ArrayList<>(deps.size());
        for (URL url : deps) {
            urlStrs.add(url.toString());
        }
        assertThat(urlStrs, CONTAINS_IN_ANY_ORDER_DEPS);
        // checks for wrong url protocol
        assertThat(DependenciesReader.extractDependencies(new URL("file:foo")), is(empty()));
        // checks for null url
        assertThat(DependenciesReader.extractDependencies(null), is(empty()));
    }

}
