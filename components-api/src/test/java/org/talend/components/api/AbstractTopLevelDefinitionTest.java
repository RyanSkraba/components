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
package org.talend.components.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.daikon.NamedThing;

/**
 * created by pbailly on 5 Nov 2015 Detailled comment
 *
 */
public class AbstractTopLevelDefinitionTest {

    class TestingAbstractTopLevelDefinition extends AbstractTopLevelDefinition {

        @Override
        public String getName() {
            return "TestName";
        }

        @Override
        protected String getI18nPrefix() {
            return "TestPrefix";
        }
    }

    @Ignore
    @Test
    public void test() {
        AbstractTopLevelDefinition atld = new TestingAbstractTopLevelDefinition();
        assertEquals("TestName", atld.getName());
        assertEquals("TestPrefix", atld.getI18nPrefix());
        assertEquals("TestPrefixTestName", atld.getI18nMessageFormatter());
        assertEquals("TestPrefixTestName", atld.getDisplayName());
        assertEquals("TestPrefixTestName", atld.getTitle());
    }

    @Test
    public void testGetTitle() {

        // check getTitle with proper i18n
        NamedThing i18nDefinition = getMockI18nDef();
        when(i18nDefinition.getI18nMessage("definition.foo.title")).thenReturn("ZeTitle");
        assertEquals("ZeTitle", i18nDefinition.getTitle());

        // check getTitle with no i18n but one available for displayname
        i18nDefinition = getMockI18nDef();
        when(i18nDefinition.getI18nMessage("definition.foo.displayName")).thenReturn("ZedisplayName");
        assertEquals("ZedisplayName", i18nDefinition.getTitle());

        // check getTitle with no i18n and no i18n for display name
        i18nDefinition = getMockI18nDef();
        assertEquals("definition.foo.title", i18nDefinition.getTitle());
    }

    private NamedThing getMockI18nDef() {
        AbstractTopLevelDefinition definition = spy(AbstractTopLevelDefinition.class);
        when(definition.getName()).thenReturn("foo");
        when(definition.getI18nPrefix()).thenReturn("definition.");
        return definition;
    }

}
