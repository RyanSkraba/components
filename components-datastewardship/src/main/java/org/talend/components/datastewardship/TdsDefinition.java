// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship;

import org.talend.components.api.component.AbstractComponentDefinition;

/**
 * Data Stewardship definition common class. It contains some common definition implementation for all Data Stewardship
 * components
 * 
 */
public abstract class TdsDefinition extends AbstractComponentDefinition {

    /**
     * Constructor sets component name
     * 
     * @param componentName component name
     */
    public TdsDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Talend Data Stewardship" }; //$NON-NLS-1$
    }

    /**
     * Returns <code>true</code> to specify that data schema should be propagated to other component without prompt.
     * <br>
     *
     * @return <code>true</code>
     */
    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

}
