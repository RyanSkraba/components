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
package org.talend.components.api.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.service.ComponentService;
import org.junit.rules.TestName;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractComponentTest {

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

    @Test
    public void testAllRuntime() {
        ComponentTestUtils.testAllRuntimeAvaialble(getComponentService());
    }

    public static Map<String, Object> getConsolidatedResults(Result result, Writer writer) {
        List<Result> results = new ArrayList();
        results.add(result);
        Map<String, Object> resultMap = writer.getWriteOperation().finalize(results, null);
        return resultMap;
    }

}
