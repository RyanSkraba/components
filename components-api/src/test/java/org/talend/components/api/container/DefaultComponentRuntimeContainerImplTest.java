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
package org.talend.components.api.container;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DefaultComponentRuntimeContainerImplTest {

    @Test
    public void testFormatDate() throws ParseException {
        DefaultComponentRuntimeContainerImpl runtimeContainer = new DefaultComponentRuntimeContainerImpl();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        Date fixedDate = format.parse("02-01-1970 04:46:40");
        assertEquals("02-01-1970", runtimeContainer.formatDate(fixedDate, "dd-MM-yyyy"));
        assertEquals("02-01-1970 04:46:40", runtimeContainer.formatDate(fixedDate, "dd-MM-yyyy hh:mm:ss"));
    }

}
