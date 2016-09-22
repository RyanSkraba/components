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
package org.talend.components.api.component.runtime;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.jar.JarInputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ops4j.pax.url.mvn.Handler;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.ops4j.pax.url.mvn.ServiceConstants;

public class JarRuntimeInfoTest {

    @BeforeClass
    public static void setupMavenUrlHandler() {
        try {
            new URL("mvn:foo/bar");
        } catch (MalformedURLException e) {
            // handles mvn local repository
            String mvnLocalRepo = System.getProperty("maven.repo.local");
            if (mvnLocalRepo != null) {
                System.setProperty("org.ops4j.pax.url.mvn.localRepository", mvnLocalRepo);
            }
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {

                @Override
                public URLStreamHandler createURLStreamHandler(String protocol) {
                    if (ServiceConstants.PROTOCOL.equals(protocol)) {
                        return new Handler();
                    } else {
                        return null;
                    }
                }
            });
        }
    }

    /**
     * Test method for {@link org.talend.components.api.component.runtime.JarRuntimeInfo#getMavenUrlDependencies()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetMavenUrlDependencies() throws IOException {
        MavenResolver mavenResolver = MavenResolvers.createMavenResolver(null, "foo");
        File jarWithDeps = mavenResolver.resolve("mvn:org.talend.components/components-api-full-example/0.1.0");
        // the artifact id used to compute the file path is different from the actual artifact ID.
        // I don't know why but this does not matter.
        JarRuntimeInfo jarRuntimeInfo = new JarRuntimeInfo(jarWithDeps.toURI().toURL(),
                DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-full-example"), null);
        List<URL> mavenUrlDependencies = jarRuntimeInfo.getMavenUrlDependencies();
        checkFullExampleDependencies(mavenUrlDependencies);
    }

    public void checkFullExampleDependencies(List<URL> mavenUrlDependencies) throws MalformedURLException {
        assertThat(mavenUrlDependencies,
                containsInAnyOrder(new URL("mvn:org.apache.avro/avro/1.8.0/jar"),
                        new URL("mvn:net.sourceforge.javacsv/javacsv/2.0/jar"),
                        new URL("mvn:com.cedarsoftware/json-io/4.4.1-SNAPSHOT/jar"), new URL("mvn:joda-time/joda-time/2.8.2/jar"),
                        new URL("mvn:org.xerial.snappy/snappy-java/1.1.1.3/jar"), new URL(
                                "mvn:org.talend.components/components-api/0.13.1/jar"),
                new URL("mvn:com.thoughtworks.paranamer/paranamer/2.7/jar"), new URL("mvn:org.talend.daikon/daikon/0.12.1/jar"),
                new URL("mvn:com.fasterxml.jackson.core/jackson-annotations/2.5.3/jar"),
                new URL("mvn:com.fasterxml.jackson.core/jackson-core/2.5.3/jar"),
                new URL("mvn:org.codehaus.jackson/jackson-core-asl/1.9.13/jar"),
                new URL("mvn:org.talend.components/components-common/0.13.1/jar"),
                new URL("mvn:biz.aQute.bnd/annotation/2.4.0/jar"), new URL("mvn:org.slf4j/slf4j-api/1.7.12/jar"),
                new URL("mvn:org.talend.components/components-api-full-example/0.1.0/jar"), new URL("mvn:org.tukaani/xz/1.5/jar"),
                new URL("mvn:javax.inject/javax.inject/1/jar"), new URL("mvn:org.apache.commons/commons-compress/1.8.1/jar"),
                new URL("mvn:org.apache.commons/commons-lang3/3.4/jar"), new URL("mvn:javax.servlet/javax.servlet-api/3.1.0/jar"),
                new URL("mvn:commons-codec/commons-codec/1.6/jar"),
                new URL("mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.13/jar"))//
        );
    }

    /**
     * Test method for
     * {@link org.talend.components.api.component.runtime.JarRuntimeInfo#extracDependencyFromStream(org.talend.components.api.component.runtime.DependenciesReader, java.lang.String, java.util.jar.JarInputStream)}
     * .
     * 
     * @throws IOException
     * @throws MalformedURLException
     */
    @Test
    public void testExtracDependencyFromStream() throws MalformedURLException, IOException {
        MavenResolver mavenResolver = MavenResolvers.createMavenResolver(null, "foo");
        File jarWithDeps = mavenResolver.resolve("mvn:org.talend.components/components-api-full-example/0.1.0");
        try (JarInputStream jis = new JarInputStream(new FileInputStream(jarWithDeps))) {
            List<URL> dependencyFromStream = JarRuntimeInfo.extracDependencyFromStream(new DependenciesReader(null),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-full-example"), jis);
            checkFullExampleDependencies(dependencyFromStream);

        }
    }

    /**
     * Test method for {@link org.talend.components.api.component.runtime.JarRuntimeInfo#getRuntimeClassName()}.
     */
    @Test
    public void testGetRuntimeClassName() {
        JarRuntimeInfo jarRuntimeInfo = new JarRuntimeInfo(null, null, "foo");
        assertEquals("foo", jarRuntimeInfo.getRuntimeClassName());
    }

}
