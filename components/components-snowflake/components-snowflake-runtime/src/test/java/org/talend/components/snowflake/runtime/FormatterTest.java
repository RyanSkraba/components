// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.junit.Test;

public class FormatterTest {

    @Test
    public void testFormatter4SafeThread() {
        final Formatter f1 = new Formatter();
        new Thread(new Runnable() {

            @Override
            public void run() {
                Formatter f2 = new Formatter();
                compare(f1,f2);
            }

        }).start();
    }

    public void compare(Formatter f1, Formatter f2) {
        Assert.assertNotEquals(f1.getDateFormatter(), f2.getDateFormatter());
        Assert.assertNotEquals(f1.getTimeFormatter(), f2.getTimeFormatter());
        Assert.assertNotEquals(f1.getTimestampFormatter(), f2.getTimestampFormatter());
    }
}
