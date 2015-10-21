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
package org.talend.components.api.service.testcomponent.nestedprop;

import static org.talend.components.api.schema.SchemaFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;

public class NestedComponentProperties extends ComponentProperties {

    public static final String A_GREAT_PROP_NAME = "aGreatProp"; //$NON-NLS-1$

    public SchemaElement aGreatProperty = newProperty(A_GREAT_PROP_NAME);

}