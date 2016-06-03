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
package org.talend.components.salesforce.runtime;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedSource;

public class SalesforceSourceTest {

    @Test
    public void testSplitIntoBundles() throws Exception {
        SalesforceSource salesforceSource = new SalesforceSource();
        List<? extends BoundedSource> bundles = salesforceSource.splitIntoBundles(12, null);
        assertEquals(1, bundles.size());
        assertEquals(salesforceSource, bundles.get(0));
    }

    @Test
    public void testGetEstimatedSizeBytes() throws Exception {
        assertEquals(0, new SalesforceSource().getEstimatedSizeBytes(null));
    }

    @Test
    public void testProducesSortedKeys() throws Exception {
        assertFalse(new SalesforceSource().producesSortedKeys(null));
    }

    @Test
    public void testCreateReader() throws IOException {
        SalesforceSource salesforceSource = new SalesforceSource();
        assertNull(salesforceSource.createReader(null));
    }

}
