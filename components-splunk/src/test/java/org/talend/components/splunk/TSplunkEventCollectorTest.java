package org.talend.components.splunk;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

@SuppressWarnings("nls")
public class TSplunkEventCollectorTest extends TSplunkEventCollectorTestBase {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Ignore
    @Test
    public void testtSplunkEventCollectorRuntime() throws Exception {
    }

    @Ignore("To revisit.  The spec should be validated by the time it gets to the runtime.")
    @Test
    public void testtSplunkEventCollectorRuntimeException() {
    }

}
