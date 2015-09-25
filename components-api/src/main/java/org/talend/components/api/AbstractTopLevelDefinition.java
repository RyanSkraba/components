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
package org.talend.components.api;

import org.talend.components.api.context.GlobalContext;
import org.talend.components.api.i18n.TranslatableImpl;

/**
 * created by sgandon on 21 sept. 2015
 */
public abstract class AbstractTopLevelDefinition extends TranslatableImpl implements TopLevelDefinition {

    private static final String I18N_DISPLAY_NAME_SUFFIX = ".displayName"; //$NON-NLS-1$

    private static final String I18N_TITLE_SUFFIX = ".title"; //$NON-NLS-1$

    protected GlobalContext globalContext;

    // @Override
    // public void setGlobalContext(GlobalContext globalContext) {
    // this.globalContext = globalContext;
    // setI18nMessageFormater(
    // globalContext.i18nMessageProvider.getI18nMessages(this.getClass().getClassLoader(), getI18NBaseName()));
    // }
    //
    // /**
    // * This is used to create a I18nMessages instance for I18N texts
    // *
    // * @return the baseName used to locate the ResourceBundle like defined in
    // * {@link java.util.ResourceBundle#getBundle(String, java.util.Locale, ClassLoader,
    // java.util.ResourceBundle.Control)}
    // * , something like "org.something.MyMessage"
    // */
    // protected abstract String getI18NBaseName();

    // /**
    // * return the Internationnalised messages found from the resource found with the provided baseName from
    // * getI18NBaseName()
    // *
    // * @param key the key to identify the message
    // * @param arguments the arguments that shall be used to format the message using
    // * {@link java.text.MessageFormat#format(String, Object...))}
    // * @return the formated string or the key if no message was found
    // */
    // @Override
    // public String getI18nMessage(String key, Object... arguments) {
    // if (i18nMessages == null) {
    // String className = this.getClass().getCanonicalName();
    // String baseName = className.substring(0, className.lastIndexOf('.')) + ".messages";
    // i18nMessages = GlobalContext.i18nMessageProvider.getI18nMessages(this.getClass().getClassLoader(), baseName);
    // }
    // return i18nMessages.getMessage(key, arguments);
    // }

    @Override
    public String getDisplayName() {
        return getI18nMessage(getI18nPrefix() + getName() + I18N_DISPLAY_NAME_SUFFIX);
    }

    @Override
    public String getTitle() {
        return getI18nMessage(getI18nPrefix() + getName() + I18N_TITLE_SUFFIX);
    }

    abstract protected String getI18nPrefix();
}
