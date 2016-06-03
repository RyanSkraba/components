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

import org.junit.Test;

public class ComponentServiceImplTest {

    /**
     * Test method for parseMvnUri
     * {@link org.talend.components.api.service.internal.ComponentServiceImpl#parseMvnUri(java.lang.String)} .
     */
    @Test
    public void testparseMvnUri() {
        ComponentServiceImpl componentServiceImpl = new ComponentServiceImpl(null);
        String parsedMvnUri = componentServiceImpl
                .parseMvnUri("     org.talend.components:components-api:test-jar:tests:0.4.0.BUILD-SNAPSHOT:test");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/test-jar/tests", parsedMvnUri);
        parsedMvnUri = componentServiceImpl
                .parseMvnUri("    org.talend.components:components-api:jar:0.4.0.BUILD-SNAPSHOT:compile   ");
        assertEquals("mvn:org.talend.components/components-api/0.4.0.BUILD-SNAPSHOT/jar", parsedMvnUri);
    }

}
