// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.daikon.properties.test.AbstractPropertiesTest;

/**
 * base class for generic Integration tests that will perform I18N checks for all definitions and properties as well as image
 * checks.
 */
public abstract class AbstractComponentTest2 extends AbstractPropertiesTest {

    // for benchmarking the apis, one suggestion is to use http://openjdk.java.net/projects/code-tools/jmh/.

    @Override
    @Test
    public void testAlli18n() {
        super.testAlli18n();
        ComponentTestUtils.assertReturnProperties18nAreSet(getDefinitionRegistry(), errorCollector);
    }

    @Ignore
    @Override
    @Test
    public void testAllImages() {
        super.testAllImages();
        ComponentTestUtils.assertAllComponentImagesAreSet(getDefinitionRegistry());
    }

    public static Map<String, Object> getConsolidatedResults(Result result, Writer writer) {
        List<Result> results = new ArrayList();
        results.add(result);
        Map<String, Object> resultMap = writer.getWriteOperation().finalize(results, null);
        return resultMap;
    }

}
