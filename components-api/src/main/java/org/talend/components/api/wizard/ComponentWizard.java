package org.talend.components.api.wizard;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.i18n.TranslatableImpl;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;

/**
 * A component wizard is used to create {@link ComponentProperties } objects
 */
public abstract class ComponentWizard extends TranslatableImpl {

    protected String userData;

    protected List<Form> forms;

    protected I18nMessageProvider i18nMessageProvider;

    /**
     * inheriting class must call i18nMessagesProvider at the end of the constructor and every time they create and new
     * direct property
     * 
     * @param messageProvider, used to find the I18nMessage according to the current LocalProvider
     * @param baseName, used to find the resource file for I18N
     */
    public ComponentWizard(String userData, I18nMessageProvider i18nMessageProvider, String baseName) {
        this.i18nMessageProvider = i18nMessageProvider;
        if (i18nMessageProvider != null) {
            setI18nMessageFormater(i18nMessageProvider.getI18nMessages(this.getClass().getClassLoader(), baseName));
        }
        forms = new ArrayList<>();
        this.userData = userData;
    }

    public void addForm(Form form) {
        forms.add(form);
    }

    public List<Form> getForms() {
        return forms;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }
}
