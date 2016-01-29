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
package org.talend.components.api.properties;

import org.talend.components.api.NamedThing;

/**
 * created by sgandon on 29 janv. 2016
 */
public interface AnyProperty extends NamedThing {

    /**
     * Offers a visiting pattern to this object
     * 
     * @param visitor
     */
    public void accept(AnyPropertyVisitor visitor);
}
