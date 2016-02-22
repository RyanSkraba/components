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
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

public class SalesforceSourceTest {

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSource#splitIntoBundles(long, org.talend.components.api.adaptor.Adaptor)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testSplitIntoBundles() throws Exception {
        SalesforceSource salesforceSource = new SalesforceSource();
        List<? extends BoundedSource> bundles = salesforceSource.splitIntoBundles(12, null);
        assertEquals(1, bundles.size());
        assertEquals(salesforceSource, bundles.get(0));
    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSource#getEstimatedSizeBytes(org.talend.components.api.adaptor.Adaptor)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testGetEstimatedSizeBytes() throws Exception {
        assertEquals(0, new SalesforceSource().getEstimatedSizeBytes(null));
    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSource#producesSortedKeys(org.talend.components.api.adaptor.Adaptor)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testProducesSortedKeys() throws Exception {
        assertFalse(new SalesforceSource().producesSortedKeys(null));
    }

    /**
     * Test method for
     * {@link org.talend.components.salesforce.runtime.SalesforceSource#createReader(org.talend.components.api.adaptor.Adaptor)}
     * .
     * 
     * @throws IOException
     */
    @Test
    public void testCreateReader() throws IOException {
        SalesforceSource salesforceSource = new SalesforceSource();
        assertNull(salesforceSource.createReader(null));
        // initialize it
        salesforceSource.initialize(null, new TSalesforceInputProperties(null));
        assertNotNull(salesforceSource);
    }

}
