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
package org.talend.components.api.test;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsApiErrorCode;
import org.talend.components.api.service.ComponentService;

/**
 * @deprecated please use the {@link AbstractComponentTest2} instead
 */
@Deprecated
public abstract class AbstractComponentTest {

    static {
        new JarRuntimeInfo((URL) null, null, null); // ensure mvn url - pax url - is registered
        if (System.getProperty("sun.boot.class.path") == null) { // j11 workaround due to daikon
            System.setProperty("sun.boot.class.path", System.getProperty("java.class.path"));
        }
    }

    // for benchmarking the apis, one suggestion is to use http://openjdk.java.net/projects/code-tools/jmh/.
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    abstract public ComponentService getComponentService();

    @Test
    public void testAlli18n() {
        ComponentTestUtils.testAlli18n(getComponentService(), errorCollector);
    }

    @Test
    public void testAllImages() {
        ComponentTestUtils.testAllImages(getComponentService());
    }

    protected void assertComponentIsRegistered(String componentName) {
        try {
            ComponentDefinition componentDefinition = getComponentService().getComponentDefinition(componentName);
            assertNotNull(componentDefinition);
        } catch (ComponentException ce) {
            if (ce.getCode() == ComponentsApiErrorCode.WRONG_COMPONENT_NAME) {
                fail("Could not find component [], please check the registered component familly is in package org.talend.components");
            } else {
                throw ce;
            }
        }
    }

    public static Map<String, Object> getConsolidatedResults(Result result, Writer writer) {
        List<Result> results = new ArrayList();
        results.add(result);
        Map<String, Object> resultMap = writer.getWriteOperation().finalize(results, null);
        return resultMap;
    }

}
