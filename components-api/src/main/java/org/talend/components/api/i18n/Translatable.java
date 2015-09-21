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

import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.daikon.i18n.I18nMessages;

/**
 * created by sgandon on 18 sept. 2015
 */
public interface Translatable {

    void setI18nMessageFormater(I18nMessages i18nMessages);

    /**
     * return the Internationnalised messages found from the resource found with the provided baseName from
     * getI18NBaseName()
     * 
     * @param key the key to identify the message
     * @param arguments the arguments that shall be used to format the message using
     * {@link java.text.MessageFormat#format(String, Object...))}
     * @return the formated string or the key if no message was found
     * @Exception ComponentException thrown with the code {@link ComponentsErrorCode#MISSING_I18N_TRANSLATOR} if the
     * I18nMessages that should have been set by {@link Translatable#setI18NMessageFormater(I18nMessages)} is null
     */
    String getI18nMessage(String key, Object... arguments);

}