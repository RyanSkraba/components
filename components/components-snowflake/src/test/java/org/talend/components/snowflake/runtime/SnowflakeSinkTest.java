package org.talend.components.snowflake.runtime;

import org.junit.Assert;
import org.junit.Test;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class SnowflakeSinkTest {

    @Test
    public void testI18NMessages() {
        I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeSink.class);
        String wrongPropertiesTypeMessage = i18nMessages.getMessage("debug.wrongPropertiesType");

        Assert.assertFalse(wrongPropertiesTypeMessage.equals("debug.wrongPropertiesType"));
    }
}
