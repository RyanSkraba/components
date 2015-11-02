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
package org.talend.components;

import static org.junit.Assert.*;

import java.util.List;

import org.talend.components.api.NamedThing;
import org.talend.components.api.context.GlobalContext;
import org.talend.components.api.i18n.I18nMessageProvider;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentProperties.Deserialized;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;

/**
 * created by sgandon on 27 oct. 2015
 */
public class ComponentTestUtils {

    public static ComponentProperties checkSerialize(ComponentProperties props) {
        String s = props.toSerialized();
        ComponentProperties.Deserialized d = ComponentProperties.fromSerialized(s);
        ComponentProperties deserProps = d.properties;
        assertFalse(d.migration.isMigrated());
        List<SchemaElement> newProps = deserProps.getProperties();
        List<Form> newForms = deserProps.getForms();
        int i = 0;
        for (NamedThing prop : props.getProperties()) {
            System.out.println(prop.getName());
            assertEquals(prop.getName(), newProps.get(i).getName());
            i++;
        }
        i = 0;
        for (Form form : props.getForms()) {
            System.out.println("Form: " + form.getName());
            Form newForm = newForms.get(i++);
            assertEquals(form.getName(), form.getName());
            for (NamedThing formChild : form.getChildren()) {
                String name = formChild.getName();
                if (formChild instanceof Form) {
                    name = ((Form) formChild).getProperties().getName();
                }
                System.out.println("  prop: " + formChild.getName() + " name to be used: " + name);
                NamedThing newChild = newForm.getChild(name);
                String newName = newChild.getName();
                if (newChild instanceof Form) {
                    newName = ((Form) newChild).getProperties().getName();
                }
                assertEquals(name, newName);
            }
        }
        return deserProps;
        
    }

    /**
     * DOC sgandon Comment method "setupGlobalContext".
     */
    public static void setupGlobalContext() {
        I18nMessageProvider i18nMessageProvider = new I18nMessageProvider();
        i18nMessageProvider.osgiInjectLocalProvider(null);
        GlobalContext.i18nMessageProvider = i18nMessageProvider;
    }

    /**
     * DOC sgandon Comment method "unsetGlobalContext".
     */
    public static void unsetGlobalContext() {
        GlobalContext.i18nMessageProvider = null;
    }

}
