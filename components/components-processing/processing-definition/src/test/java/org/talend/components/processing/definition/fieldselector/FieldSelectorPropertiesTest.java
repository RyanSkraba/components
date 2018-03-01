// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.processing.definition.fieldselector;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;

public class FieldSelectorPropertiesTest {

    @Test
    public void testDefaultProperties() {
        FieldSelectorProperties properties = new FieldSelectorProperties("test");
        properties.init(); 
        assertThat(properties.selectors.getDefaultProperties(), notNullValue());
        assertThat(properties.getAllSchemaPropertiesConnectors(true), contains(properties.OUTGOING_CONNECTOR));
        assertThat(properties.getAllSchemaPropertiesConnectors(false), contains(properties.INCOMING_CONNECTOR));
        
    }

    @Test
    public void testSelectorsProperties() {
        FieldSelectorProperties properties = new FieldSelectorProperties("test");
        properties.init();
        SelectorProperties selector = new SelectorProperties("test");
        selector.field.setValue("myField");
        selector.path.setValue(".myPath");
        selector.init();
        properties.selectors.addRow(selector);
        assertThat(properties.selectors.subProperties.size(), equalTo(2));
        assertThat(properties.selectors.subProperties.get(0).field.getValue(), equalTo(""));
        assertThat(properties.selectors.subProperties.get(0).path.getValue(), equalTo(""));
        assertThat(properties.selectors.subProperties.get(1).field.getValue(), equalTo("myField"));
        assertThat(properties.selectors.subProperties.get(1).path.getValue(), equalTo(".myPath"));
        
    }
}
