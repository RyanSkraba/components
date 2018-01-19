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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit-tests for {@link MappingFileLoader}
 */
public class MappingFileLoaderTest {

    @Test
    public void testLoad() {
        File mappingFile = new File(getClass().getResource("mapping_mysql.xml").getPath());
        MappingFileLoader fileLoader = new MappingFileLoader();
        List<Dbms> dbmsList = fileLoader.load(mappingFile);

        assertThat(dbmsList, hasSize(1));
        Dbms dbms = dbmsList.get(0);
        assertThat(dbms.getDbmsTypes(), hasSize(43));
        assertThat(dbms.getDbmsTypes(),
                containsInAnyOrder("BIGINT", "BIGINT UNSIGNED", "BINARY", "BIT", "BLOB", "CHAR", "DATE", "DATETIME", "DECIMAL",
                        "DOUBLE", "DOUBLE UNSIGNED", "ENUM", "FLOAT", "FLOAT UNSIGNED", "GEOMETRY", "GEOMETRYCOLLECTION", "INT",
                        "INT UNSIGNED", "LINESTRING", "LONGTEXT", "LONGBLOB", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMINT UNSIGNED",
                        "MEDIUMTEXT", "MULTILINESTRING", "MULTIPOINT", "MULTIPOLYGON", "POINT", "POLYGON", "SMALLINT",
                        "SMALLINT UNSIGNED", "SET", "TEXT", "TIME", "TIMESTAMP", "TINYBLOB", "TINYINT", "TINYINT UNSIGNED",
                        "TINYTEXT", "VARBINARY", "VARCHAR", "YEAR"));

        DbmsType blobType = dbms.getDbmsType("BLOB");
        Assert.assertTrue(blobType.isIgnoreLength());
        Assert.assertTrue(blobType.isIgnorePrecision());

        DbmsType decimalType = dbms.getDbmsType("DECIMAL");
        Assert.assertEquals(20, decimalType.getDefaultLength());
        Assert.assertEquals(10, decimalType.getDefaultPrecision());

        MappingType<TalendType, DbmsType> stringMapping = dbms.getTalendMapping("id_String");
        Assert.assertEquals(TalendType.STRING, stringMapping.getSourceType());
        Assert.assertEquals("VARCHAR", stringMapping.getDefaultType().getName());

    }

}
