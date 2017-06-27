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
package org.talend.components.snowflake.runtime;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * Unit tests for {@link SnowflakeSource} class
 */
public class SnowflakeSourceTest {

    private SnowflakeSource source;

    @Before
    public void setup() {
        source = new SnowflakeSource();
        source.initialize(null, new TSnowflakeInputProperties("inputProperties"));
    }

    @Test
    public void testSplitIntoBundles() {
        Assert.assertEquals(Collections.singletonList(source), source.splitIntoBundles(0, null));
    }

    @Test
    public void testGetEstimatedSizeBytes() {
        Assert.assertEquals(0, source.getEstimatedSizeBytes(null));
    }

    @Test
    public void testProducesSortedKeys() {
        Assert.assertFalse(source.producesSortedKeys(null));
    }

    @Test
    public void testCreateReader() {
        Assert.assertTrue(source.createReader(null) instanceof SnowflakeReader);
    }

    @Test(expected = TalendRuntimeException.class)
    public void testInvalidProperties() {
        source.initialize(null, null);
        source.createReader(null);
    }
}
