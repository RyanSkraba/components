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
package org.talend.components.common;

import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.i18n.tag.TagImpl;

/**
 * Interface containing common tags for different component implementations.
 */
public interface CommonTags {

    static final I18nMessages I18_N_MESSAGES = GlobalI18N.getI18nMessageProvider().getI18nMessages(CommonTags.class);

    public static final TagImpl CLOUD_TAG = new TagImpl("cloud", null, I18_N_MESSAGES);

    public static final TagImpl BUSINESS_TAG = new TagImpl("business", null, I18_N_MESSAGES);

}
