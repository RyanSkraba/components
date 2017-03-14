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
package org.talend.components.marketo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.talend.components.marketo.MarketoComponentDefinition.MAVEN_ARTIFACT_ID;
import static org.talend.components.marketo.MarketoComponentDefinition.MAVEN_GROUP_ID;
import static org.talend.components.marketo.MarketoComponentDefinition.MAVEN_PATH;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.url.mvn.Handler;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkSchemaProvider;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionDefinition;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class MarketoRuntimeInfoTest {

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoRuntimeInfoTest.class);

    @BeforeClass
    public static void setupMavenUrlHandler() {
        try {
            new URL("mvn:foo/bar");
        } catch (MalformedURLException e) {
            // handles mvn local repository
            String mvnLocalRepo = System.getProperty("maven.repo.local");
            LOG.info("local repos {}", mvnLocalRepo);
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

    @Test
    public void testSandbox() throws Exception {
        RuntimeInfo runtimeInfo = MarketoComponentDefinition.getCommonRuntimeInfo(this.getClass().getClassLoader(),
                MarketoComponentDefinition.RUNTIME_SOURCE_CLASS);
        SandboxedInstance sandbox = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader());
        MarketoSourceOrSinkSchemaProvider ss = (MarketoSourceOrSinkSchemaProvider) sandbox.getInstance();
        ss.initialize(null, new TMarketoInputProperties(""));
        assertNull(ss.getSchemaForParams(null));
    }

    public void checkFullExampleDependencies(List<URL> mavenUrlDependencies) throws MalformedURLException {
        assertThat(mavenUrlDependencies,
                containsInAnyOrder(//
                        new URL("mvn:org.apache.commons/commons-compress/1.8.1/jar"), //
                        new URL("mvn:org.xerial.snappy/snappy-java/1.1.1.3/jar"), //
                        new URL("mvn:com.fasterxml.jackson.core/jackson-annotations/2.5.3/jar"), //
                        new URL("mvn:com.fasterxml.jackson.core/jackson-core/2.5.3/jar"), //
                        new URL("mvn:com.thoughtworks.paranamer/paranamer/2.7/jar"), //
                        new URL("mvn:biz.aQute.bnd/annotation/2.4.0/jar"), //
                        new URL("mvn:joda-time/joda-time/2.8.2/jar"), //
                        new URL("mvn:org.talend.components/components-marketo/0.18.0-SNAPSHOT/jar"), //
                        new URL("mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.13/jar"), //
                        new URL("mvn:com.cedarsoftware/json-io/4.9.9-TALEND/jar"), //
                        new URL("mvn:javax.servlet/javax.servlet-api/3.1.0/jar"), //
                        new URL("mvn:com.fasterxml.jackson.core/jackson-databind/2.5.3/jar"), //
                        new URL("mvn:org.apache.avro/avro/1.8.1/jar"), //
                        new URL("mvn:net.sourceforge.javacsv/javacsv/2.0/jar"), //
                        new URL("mvn:org.tukaani/xz/1.5/jar"), //
                        new URL("mvn:org.codehaus.jackson/jackson-core-asl/1.9.13/jar"), //
                        new URL("mvn:org.slf4j/slf4j-api/1.7.12/jar"), //
                        new URL("mvn:org.talend.daikon/daikon/0.16.0-SNAPSHOT/jar"), //
                        new URL("mvn:org.talend.components/components-common/0.18.0-SNAPSHOT/jar"), //
                        new URL("mvn:com.google.code.gson/gson/2.8.0/jar"), //
                        new URL("mvn:commons-codec/commons-codec/1.6/jar"), //
                        new URL("mvn:org.talend.components/components-api/0.18.0-SNAPSHOT/jar"), //
                        new URL("mvn:org.talend.libraries/marketo-soap-sdk/2.7/jar"), //
                        new URL("mvn:javax.inject/javax.inject/1/jar"), //
                        new URL("mvn:org.apache.commons/commons-lang3/3.4/jar")//
                ));
    }

    // FIXME move it to a integration test module
    @Ignore
    @Test
    public void testGetMavenUrlDependencies() throws IOException {
        MavenResolver mavenResolver = MavenResolvers.createMavenResolver(null, "foo");
        File jarWithDeps = mavenResolver.resolve(MAVEN_PATH);
        // the artifact id used to compute the file path is different from the actual artifact ID. I don't know why but
        // this does not matter.
        JarRuntimeInfo jarRuntimeInfo = new JarRuntimeInfo(jarWithDeps.toURI().toURL(),
                DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID), null);
        List<URL> mavenUrlDependencies = jarRuntimeInfo.getMavenUrlDependencies();
        checkFullExampleDependencies(mavenUrlDependencies);
    }

    // FIXME move it to a integration test module
    @Ignore
    @Test
    public void testExtracDependencyFromStream() throws IOException {
        MavenResolver mavenResolver = MavenResolvers.createMavenResolver(null, "foo");
        File jarWithDeps = mavenResolver.resolve(MAVEN_PATH + "/0.18.0-SNAPSHOT");
        try (JarInputStream jis = new JarInputStream(new FileInputStream(jarWithDeps))) {
            List<URL> dependencyFromStream = extractDependencyFromStream(new DependenciesReader(null),
                    DependenciesReader.computeDependenciesFilePath(MAVEN_GROUP_ID, MAVEN_ARTIFACT_ID), jis);
            checkFullExampleDependencies(dependencyFromStream);
        }
    }

    static List<URL> extractDependencyFromStream(DependenciesReader dependenciesReader, String depTxtPath,
            JarInputStream jarInputStream) throws IOException {
        JarEntry nextJarEntry = jarInputStream.getNextJarEntry();
        while (nextJarEntry != null) {
            if (depTxtPath.equals(nextJarEntry.getName())) {// we got it so parse it.
                Set<String> dependencies = dependenciesReader.parseDependencies(jarInputStream);
                // convert the string to URL
                List<URL> result = new ArrayList<>(dependencies.size());
                for (String urlString : dependencies) {
                    result.add(new URL(urlString));
                    System.out.println("new URL(\"" + urlString + "\"),//");
                }
                return result;
            }
            nextJarEntry = jarInputStream.getNextJarEntry();
        }
        throw new ComponentException(ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED,
                ExceptionContext.withBuilder().put("path", depTxtPath).build());
    }

    @Test
    public void testJIRA_TUP17080() {
        TMarketoConnectionDefinition def = new TMarketoConnectionDefinition();
        TMarketoConnectionProperties props = new TMarketoConnectionProperties("tests");
        RuntimeInfo runtimeInfo = def.getRuntimeInfo(ExecutionEngine.DI, props, ConnectorTopology.OUTGOING);
        assertNull(runtimeInfo);
        runtimeInfo = def.getRuntimeInfo(ExecutionEngine.DI, props, ConnectorTopology.NONE);
        assertNotNull(runtimeInfo);
        LOG.debug("{}", runtimeInfo);
        LOG.debug("{}", runtimeInfo.getMavenUrlDependencies());
    }

}
