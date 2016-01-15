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
package org.talend.components.api.service.testcomponent;

import static org.talend.components.api.properties.PropertyFactory.*;

import org.talend.components.api.context.GlobalContext;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.daikon.i18n.I18nMessages;

public class ComponentPropertiesWithDefinedI18N extends ComponentProperties {

    public static final String A_GREAT_PROP_NAME2 = "aGreatProp2"; //$NON-NLS-1$

    public Property aGreatProp2 = newProperty(A_GREAT_PROP_NAME2);

    public ComponentPropertiesWithDefinedI18N(String name) {
        super(name);
    }

    /**
     * This uses the globalContext static variable and the current Class location to find the resource bundle names
     * messages
     * 
     * @return the already set I18nMessages or a newly created one base on the current Class package.
     */
    @Override
    protected I18nMessages createI18nMessageFormater() {
        return GlobalContext.getI18nMessageProvider().getI18nMessages(this.getClass().getClassLoader(),
                "org/talend/components/api/service/specificmessages");
    }

}