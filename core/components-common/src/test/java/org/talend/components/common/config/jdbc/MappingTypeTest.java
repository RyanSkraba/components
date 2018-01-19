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
package org.talend.components.common.config.jdbc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit-tests for {@link MappingType}
 */
public class MappingTypeTest {

    @Test
    public void testGetters() {
        TalendType expectedSourceType = TalendType.CHARACTER;
        DbmsType defaultTargetType = new DbmsType("CLOB", false, 0, 0, false, false, false);
        Set<DbmsType> alternativeTypes = new HashSet<>(Arrays.asList( //
                new DbmsType("alt1", false, 0, 0, false, false, false), //
                new DbmsType("alt2", false, 0, 0, false, false, false)));
        MappingType<TalendType, DbmsType> mappingType = new MappingType<>(expectedSourceType, defaultTargetType,
                alternativeTypes);

        Assert.assertEquals(expectedSourceType, mappingType.getSourceType());
        Assert.assertEquals(defaultTargetType, mappingType.getDefaultType());
        Assert.assertEquals(alternativeTypes, mappingType.getAdvisedTypes());
    }

}
