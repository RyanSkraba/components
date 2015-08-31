package org.talend.components.base.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LocalComponentTest extends TestCase {

    public LocalComponentTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(LocalComponentTest.class);
    }

    public void testApp() {
        assertTrue(true);
    }

}
