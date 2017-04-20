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
