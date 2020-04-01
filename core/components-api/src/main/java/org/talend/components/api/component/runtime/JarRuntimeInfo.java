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

import sun.misc.Unsafe;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.Objects;

import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.sandbox.SandboxControl;

/**
 * create a {@link RuntimeInfo} that will look for a given jar and will look for a dependency.txt file inside this jar
 * given the maven groupId and artifactID to find the right path to the file.
 */
public class JarRuntimeInfo implements RuntimeInfo, SandboxControl {

    private static final Logger LOG = LoggerFactory.getLogger(JarRuntimeInfo.class);

    private final String runtimeClassName;

    private final URL jarUrl;

    private final String depTxtPath;

    private final boolean reusable;

    static {
        try {
            new URL("mvn:foo/bar");
        } catch (MalformedURLException e) {

            // handles mvn local repository
            String mvnLocalRepo = System.getProperty("maven.repo.local");
            if (mvnLocalRepo != null && !mvnLocalRepo.isEmpty()) {
                System.setProperty("org.ops4j.pax.url.mvn.localRepository", mvnLocalRepo);
            }
            // remove warnings on illegal reflective access using Unsafe... :-D
            try {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                Unsafe unsafe = (Unsafe) theUnsafe.get(null);
                Class clazz = Class.forName("jdk.internal.module.IllegalAccessLogger");
                Field logger = clazz.getDeclaredField("logger");
                unsafe.putObjectVolatile(clazz, unsafe.staticFieldOffset(logger), null);
            } catch (Exception ue) {
                // ignore
            }
            // If the URL above failed, the mvn protocol needs to be installed.
            // not advice create a wrap URLStreamHandlerFactory class now
            try {
                final Field factoryField = URL.class.getDeclaredField("factory");
                factoryField.setAccessible(true);
                final Field lockField = URL.class.getDeclaredField("streamHandlerLock");
                lockField.setAccessible(true);

                synchronized (lockField.get(null)) {
                    final URLStreamHandlerFactory factory = (URLStreamHandlerFactory) factoryField.get(null);
                    // avoid the factory already defined error
                    if (factory == null) {
                        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {

                            @Override
                            public URLStreamHandler createURLStreamHandler(String protocol) {
                                if (!ServiceConstants.PROTOCOL.equals(protocol)) {
                                    return null;
                                }
                                return new URLStreamHandler() {
                                    @Override
                                    public URLConnection openConnection(URL url) throws IOException {
                                        MavenResolver resolver = MavenResolvers.createMavenResolver(null, ServiceConstants.PID);
                                        // java11 adds #runtime spec for (classloader) resource loading and breaks pax
                                        Connection conn = new Connection(new URL(url.toExternalForm().replace("#runtime", "")), resolver);
                                        conn.setUseCaches(false);// to avoid concurent thread to have an IllegalStateException.
                                        return conn;
                                    }

                                    @Override
                                    protected void parseURL(URL u, String spec, int start, int limit) {
                                        if (!"mvn:".equals(u.toString())) {// remove the spec to only return the url.
                                            LOG.debug("ignoring specs for parseUrl with url[" + u + "] and spec[" + spec + "]");
                                            super.parseURL(u, "", 0, 0);
                                        } else {// simple url being "mvn:" and the rest is specs.
                                            super.parseURL(u, spec, start, limit);
                                        }
                                    }
                                };
                            }

                        });
                    }
                }
            } catch (Exception exception) {
                LOG.warn(exception.getMessage());
            }
        }

        if (System.getProperty("sun.boot.class.path") == null) { // j11 workaround due to daikon
            System.setProperty("sun.boot.class.path", System.getProperty("java.class.path"));
        }
    }

    private static class Connection extends URLConnection { // forked cause not exposed through OSGi meta
        private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

        private final MavenResolver resolver;

        private Connection(final URL url, final MavenResolver resolver) {
            super( url );
            this.resolver = resolver;
        }

        @Override
        public void connect() {
            // do nothing
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            LOG.debug( "Resolving [" + url.toExternalForm() + "]" );
            return new FileInputStream(resolver.resolve(url.toExternalForm()));
        }
    }

    /**
     * uses the <code>mavenGroupId</code> <code>mavenArtifactId</code> to locate the *dependency.txt* file using the
     * rule defined in {@link DependenciesReader#computeDependenciesFilePath}
     * 
     * @param jarUrl url of the jar to read the dependency.txt from
     * @param depTxtPath, path used to locate the dependency.txt file
     * @param runtimeClassName class to be instantiated
     */
    public JarRuntimeInfo(URL jarUrl, String depTxtPath, String runtimeClassName) {
        this(jarUrl, depTxtPath, runtimeClassName, SandboxControl.CLASSLOADER_REUSABLE);
    }

    /**
     * uses the <code>mavenGroupId</code> <code>mavenArtifactId</code> to locate the *dependency.txt* file using the
     * rule defined in {@link DependenciesReader#computeDependenciesFilePath}
     *
     * @param jarUrl url of the jar to read the dependency.txt from
     * @param depTxtPath, path used to locate the dependency.txt file
     * @param runtimeClassName class to be instantiated
     * @param reusable whether the ClassLoader for the runtime instance is cacheable and reusable across calls.
     */
    public JarRuntimeInfo(URL jarUrl, String depTxtPath, String runtimeClassName, boolean reusable) {
        this.jarUrl = jarUrl;
        this.depTxtPath = depTxtPath;
        this.runtimeClassName = runtimeClassName;
        this.reusable = reusable;
    }

    /**
     * uses the <code>mavenGroupId</code> <code>mavenArtifactId</code> to locate the *dependency.txt* file using the
     * rule defined in {@link DependenciesReader#computeDependenciesFilePath}
     * 
     * @param jarUrlString url of the jar to read the dependency.txt from
     * @param depTxtPath, path used to locate the dependency.txt file
     * @param runtimeClassName class to be instantiated
     * @throws {@link TalendRuntimeException} if the jarUrlString is malformed
     */
    public JarRuntimeInfo(String jarUrlString, String depTxtPath, String runtimeClassName) {
        this(createJarUrl(jarUrlString), depTxtPath, runtimeClassName, SandboxControl.CLASSLOADER_REUSABLE);
    }

    /**
     * Constructor
     * 
     * @param jarUrlString URL of the jar to read the dependency.txt from
     * @param depTxtPath path used to locate the dependency.txt file
     * @param runtimeClassName class to be instantiated
     * @param reusable defines whether {@link ClassLoader} should be cached or not
     * @throws {@link TalendRuntimeException} if the jarUrlString is malformed
     */
    public JarRuntimeInfo(String jarUrlString, String depTxtPath, String runtimeClassName, boolean reusable) {
        this(createJarUrl(jarUrlString), depTxtPath, runtimeClassName, reusable);
    }

    private static URL createJarUrl(String jarUrlString) {
        try {
            return new URL(jarUrlString);
        } catch (MalformedURLException e) {
            LOG.debug(e.getMessage());
        }
        return null;
    }

    public JarRuntimeInfo cloneWithNewJarUrlString(String newJarUrlString) {
        return new JarRuntimeInfo(newJarUrlString, this.getDepTxtPath(), this.getRuntimeClassName());
    }

    public URL getJarUrl() {
        return jarUrl;
    }

    public String getDepTxtPath() {
        return depTxtPath;
    }

    @Override
    public List<URL> getMavenUrlDependencies() {
        return DependenciesReader.extractDepenencies(jarUrl, depTxtPath);
    }

    @Override
    public String getRuntimeClassName() {
        return runtimeClassName;
    }

    @Override
    public boolean isClassLoaderReusable() {
        return reusable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!JarRuntimeInfo.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        JarRuntimeInfo other = (JarRuntimeInfo) obj;
        // we assume that 2 list of dependencies are equals if they have the same elements in the same order cause we
        // are using them
        // as a classpath in a classloader so the order matters.
        return this.runtimeClassName.equals(other.runtimeClassName)
                && getMavenUrlDependencies().equals(other.getMavenUrlDependencies());
    }

    @Override
    public int hashCode() {
        return Objects.hash(runtimeClassName, getMavenUrlDependencies());
    }

    @Override
    public String toString() {
        return "JarRunTimeInfo: {" + "runtimeClassName:" + runtimeClassName + ", " + "jarUrl: " + jarUrl + ", " + "depTxtPath: "
                + depTxtPath + "}";
    }

}
