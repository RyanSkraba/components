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
package org.talend.components.api.i18n;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.i18n.I18nMessages;

/**
 * created by sgandon on 18 sept. 2015
 */
public class TranslatableImpl implements Translatable {

    protected I18nMessages i18nMessages;

    @Override
    public void setI18nMessageFormater(I18nMessages i18nMessages) {
        this.i18nMessages = i18nMessages;
    }

    @Override
    public String getI18nMessage(String key, Object... arguments) {
        if (i18nMessages != null) {
            return i18nMessages.getMessage(key, arguments);
        } else {
            return "Missing translator: " + key;
            // FIXME - removing this for now until I18N can support this on deserialization
            // throw new ComponentException(ComponentsErrorCode.MISSING_I18N_TRANSLATOR, ExceptionContext.build().put("key", key)); //$NON-NLS-1$
        }
    }
}
