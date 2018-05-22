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

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class SnowflakeConstantsTest {

    @Test
    public void testI18NConstants() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeConstants.class);

        String incorrectSnowflakeAccoutMessage = i18nMessages.getMessage("error.incorrectAccount");
        String connectionSuccessfullMessage = i18nMessages.getMessage("messages.success");

        assertFalse(incorrectSnowflakeAccoutMessage.equals("error.incorrectAccount"));
        assertFalse(connectionSuccessfullMessage.equals("messages.success"));
    }
}
