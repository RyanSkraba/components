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
package org.talend.components.azurestorage.blob.definitions.helpers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.blob.helpers.FileMaskTable;

public class FileMaskTableTest {

    private FileMaskTable fileMaskTable;

    List<String> masks;

    List<String> news;

    @Before
    public void setup() {
        fileMaskTable = new FileMaskTable("tests");
        masks = Arrays.asList("*", "df");
        news = Arrays.asList("FD", "LG");
    }

    /**
     *
     * @see org.talend.components.azurestorage.blob.helpers.FileMaskTable#size()
     */
    @Test
    public void testSize() {
        assertEquals(0, fileMaskTable.size());
        fileMaskTable.fileMask.setValue(masks);
        fileMaskTable.newName.setValue(news);
        assertEquals(2, fileMaskTable.size());
    }

    /**
     *
     * @see org.talend.components.azurestorage.blob.helpers.FileMaskTable#setupLayout()
     */
    @Test
    public void setupLayout() {
        fileMaskTable.setupLayout();
    }

}
