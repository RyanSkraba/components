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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

public class DependenciesReaderTest {

    /**
     * Test method for parseMvnUri
     * {@link org.talend.components.api.service.internal.ComponentServiceImpl#parseMvnUri(java.lang.String)} .
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
            assertThat(deps,
                    containsInAnyOrder("mvn:org.apache.maven/maven-core/3.3.3/jar", //
                            "mvn:org.eclipse.sisu/org.eclipse.sisu.plexus/0.0.0.M2a/jar", //
                            "mvn:org.apache.maven/maven-artifact/3.3.3/jar", //
                            "mvn:org.eclipse.aether/aether-transport-file/1.0.0.v20140518/jar", //
                            "mvn:org.talend.components/file-input/0.1.0.SNAPSHOT/jar"//
            ));
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
        assertThat(deps,
                containsInAnyOrder("mvn:org.apache.maven/maven-core/3.3.3/jar", //
                        "mvn:org.eclipse.sisu/org.eclipse.sisu.plexus/0.0.0.M2a/jar", //
                        "mvn:org.apache.maven/maven-artifact/3.3.3/jar", //
                        "mvn:org.eclipse.aether/aether-transport-file/1.0.0.v20140518/jar", //
                        "mvn:org.talend.components/file-input/0.1.0.SNAPSHOT/jar"//
        ));
    }
}
