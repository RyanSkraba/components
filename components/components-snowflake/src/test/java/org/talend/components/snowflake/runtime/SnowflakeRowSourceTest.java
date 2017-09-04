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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;

public class SnowflakeRowSourceTest {

    private Source source;

    @Before
    public void setup() {
        source = new SnowflakeRowSource();
        source.initialize(null, new TSnowflakeRowProperties("rowProperties"));
    }

    @Test
    public void testCreateReader() {
        Assert.assertTrue(source.createReader(null) instanceof SnowflakeRowReader);
    }
}
