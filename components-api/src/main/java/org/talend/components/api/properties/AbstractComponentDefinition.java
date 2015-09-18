package org.talend.components.api.properties;

import org.talend.components.api.context.GlobalContext;
import org.talend.daikon.i18n.FormatedMessage;

/**
 */
public abstract class AbstractComponentDefinition implements ComponentDefinition {

    private ComponentConnector[] connectors;

    private GlobalContext globalContext;

    private FormatedMessage formatedMessage;

    public void setConnectors(ComponentConnector... conns) {
        this.connectors = conns;
    }

    @Override
    public ComponentConnector[] getConnectors() {
        return connectors;
    }

    // FIXME - this should get it from the message file - temporary implementation
    @Override
    public String getDisplayName() {
        return getName();
    }

    // FIXME this should get it from the message file - temporary implementation
    @Override
    public String getTitle() {
        return "Title: " + getName();
    }

    @Override
    public void setGlobalContext(GlobalContext globalContext) {
        this.globalContext = globalContext;
    }

    /**
     * This is used to create a FormatedMessage instance for I18N texts
     * 
     * @return the baseName used to locate the ResourceBundle like defined in
     * {@link java.util.ResourceBundle#getBundle(String, java.util.Locale, ClassLoader, java.util.ResourceBundle.Control)}
     * , something like "org.something.MyMessage"
     */
    protected abstract String getI18NBaseName();

    /**
     * return the Internationnalised messages found from the resource found with the provided baseName from
     * getI18NBaseName()
     * 
     * @param key the key to identify the message
     * @param arguments the arguments that shall be used to format the message using
     * {@link java.text.MessageFormat#format(String, Object...))}
     * @return the formated string or the key if no message was found
     */
    protected String getI18nMessage(String key, Object... arguments) {
        if (formatedMessage == null) {
            formatedMessage = globalContext.resourceBundleProvider.getFormatedMessage(this.getClass().getClassLoader(),
                    getI18NBaseName());
        }
        return formatedMessage.getMessage(key, arguments);
    }
}
