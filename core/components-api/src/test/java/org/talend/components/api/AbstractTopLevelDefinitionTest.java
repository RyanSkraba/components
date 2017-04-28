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
package org.talend.components.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.tag.TagImpl;
import org.talend.daikon.i18n.tag.TagUtils;

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

    /**
     * Test default tag in AbstractTopLevelDefinition. Definition name is added as a default tag, if method
     * {@link AbstractTopLevelDefinition#doGetTags()} is not overriden.
     */
    @Test
    public void testGetDefaultTag() {
        AbstractTopLevelDefinition atld = new TestingAbstractTopLevelDefinition();
        Collection<TagImpl> tags = atld.getTags();
        assertEquals(1, tags.size());

        TagImpl tag = tags.iterator().next();
        assertTrue(TagUtils.hasTag(tag, "TestName"));
        assertFalse(TagUtils.hasTag(tag, "SomeOtherTag"));
    }

}
