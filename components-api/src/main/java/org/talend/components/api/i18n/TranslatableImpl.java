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

import org.talend.components.api.context.GlobalContext;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.i18n.I18nMessages;

public class TranslatableImpl implements Translatable {

    private transient I18nMessages i18nMessages;

    @Override
    public void setI18nMessageFormater(I18nMessages i18nMessages) {
        this.i18nMessages = i18nMessages;
    }

    public I18nMessages getI18nMessageFormater() {
        if (i18nMessages == null) {
            i18nMessages = createI18nMessageFormater();
        }
        return i18nMessages;
    }

    /**
     * This uses the globalContext static variable and the current Class package to find the resource bundle named
     * messages.properties (it also look into inherited class, if no key is found)
     * 
     * @return the already set I18nMessages or a newly created one base on the current Class package.
     */
    protected I18nMessages createI18nMessageFormater() {
        return GlobalContext.getI18nMessageProvider().getI18nMessages(this.getClass());
    }

    @Override
    public String getI18nMessage(String key, Object... arguments) {
        I18nMessages i18nMessageFormater = getI18nMessageFormater();
        if (i18nMessageFormater != null) {
            return i18nMessageFormater.getMessage(key, arguments);
        } else {
            // return "Missing translator: " + key;
            // FIXME - removing this for now until I18N can support this on deserialization
            throw new ComponentException(ComponentsErrorCode.MISSING_I18N_TRANSLATOR, ExceptionContext.build().put("key", key)); //$NON-NLS-1$
        }
    }
}
