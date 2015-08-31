package org.talend.components.base.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RestComponentTest extends TestCase {

    public RestComponentTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(RestComponentTest.class);
    }

    public void testApp() {
        assertTrue(true);
    }

}
