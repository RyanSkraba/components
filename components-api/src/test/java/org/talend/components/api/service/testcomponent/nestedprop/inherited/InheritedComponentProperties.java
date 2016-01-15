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
package org.talend.components.api.service.testcomponent.nestedprop.inherited;

import static org.talend.components.api.properties.PropertyFactory.*;

import org.talend.components.api.properties.Property;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;

public class InheritedComponentProperties extends NestedComponentProperties {

    public static final String A_GREAT_PROP_NAME3 = "aGreatProp3"; //$NON-NLS-1$

    public Property aGreatProp3 = newProperty(A_GREAT_PROP_NAME3);

    public InheritedComponentProperties(String name) {
        super(name);
    }
}