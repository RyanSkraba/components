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
