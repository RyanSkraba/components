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
package org.talend.components.filterrow.processing;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.talend.components.filterrow.functions.EmptyFunction;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.EqualsOperator;
import org.talend.components.filterrow.operators.OperatorType;

public class ValueProcessorTest {

    @Test
    public void testOrValueProcessor() {
        List<Filter<?, ?>> filtersString = new ArrayList<>();
        filtersString.add(new Filter<String, String>(new EmptyFunction<String>(), new EqualsOperator<String>("abc")));

        Map<String, Object> correctRecord = createObject("abc", 15);
        Map<String, Object> incorrectRecord = createObject("aad", 15);

        ValueProcessor processor = new OrValueProcessor();
        processor.setFiltersForColumn("name", filtersString);

        Assert.assertTrue(MessageFormat.format("Record {0} is correct, but processed as incorrect.", correctRecord),
                processor.process(correctRecord));
        Assert.assertFalse(MessageFormat.format("Record {0} is incorrect, but processed as correct.", incorrectRecord),
                processor.process(incorrectRecord));

    }

    @Test
    public void testAndValueProcessorWithFactory() {
        List<Filter<?, ?>> filtersForName = new ArrayList<>();
        filtersForName.add(FiltersFactory.createFilter(FunctionType.MATCH, OperatorType.EQUALS, "a.*b"));
        filtersForName.add(FiltersFactory.createFilter(FunctionType.LENGTH, OperatorType.GREATER_OR_EQUAL_TO, 4));

        List<Filter<?, ?>> filtersForInt = new ArrayList<>();
        filtersForInt.add(FiltersFactory.createFilter(FunctionType.EMPTY, OperatorType.GREATER_OR_EQUAL_TO, 10));

        Map<String, Object> correctRecord = createObject("asdfb", 15);
        Map<String, Object> incorrectRecord = createObject("def", 10);

        ValueProcessor processor = new AndValueProcessor();
        processor.setFiltersForColumn("name", filtersForName);
        processor.setFiltersForColumn("int", filtersForInt);

        Assert.assertTrue(MessageFormat.format("Record {0} is correct, but processed as incorrect.", correctRecord),
                processor.process(correctRecord));
        Assert.assertFalse(MessageFormat.format("Record {0} is incorrect, but processed as correct.", incorrectRecord),
                processor.process(incorrectRecord));
    }

    private Map<String, Object> createObject(String name, Integer intObject) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("int", intObject);
        return values;
    }
}
