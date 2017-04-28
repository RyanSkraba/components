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

import java.util.Arrays;
import java.util.List;

import org.talend.daikon.NamedThing;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.tag.TagImpl;
import org.talend.daikon.i18n.tag.TranslatableTaggedImpl;

public abstract class AbstractTopLevelDefinition extends TranslatableTaggedImpl implements NamedThing {

    private static final String I18N_DISPLAY_NAME_SUFFIX = ".displayName"; //$NON-NLS-1$

    private static final String I18N_TITLE_SUFFIX = ".title"; //$NON-NLS-1$

    protected GlobalI18N globalContext;

    @Override
    public String getDisplayName() {
        return getI18nMessage(getI18nPrefix() + getName() + I18N_DISPLAY_NAME_SUFFIX);
    }

    /**
     * return the I18N title matching the <b>[i18n_prefix].[name].title<b> key in the associated .properties message
     * where [i18n_prefix] is the value returned by {@link #getI18nPrefix()} and [name] is the value returned by
     * {@link I18nDefinition#getName()}. If no I18N was found then the {@link #getDisplayName()} is used is any is
     * provided.
     */
    @Override
    public String getTitle() {
        String title = getName() != null ? getI18nMessage(getI18nPrefix() + getName() + I18N_TITLE_SUFFIX) : "";
        if ("".equals(title) || title.startsWith(getI18nPrefix())) {
            String displayName = getDisplayName();
            if (!"".equals(displayName) && !displayName.startsWith(getI18nPrefix())) {
                title = displayName;
            } // else title is what was computed before.
        } // else title is provided so use it.
        return title;
    }

    /**
     * This implementation of {@link AbstractTopLevelDefinition#doGetTags()} adds a default tag named with definition
     * name to tags list.
     */
    @Override
    protected List<TagImpl> doGetTags() {
        return Arrays.asList(new TagImpl(getName()));
    }

    abstract protected String getI18nPrefix();
}
