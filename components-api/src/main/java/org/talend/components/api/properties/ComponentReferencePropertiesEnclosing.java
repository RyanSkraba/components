// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

/**
 * Used when {@link ComponentReferenceProperties} is used to call back to the enclosing {@link ComponentProperties}
 * after processing the associated {@link org.talend.daikon.properties.presentation.Widget}.
 */
public interface ComponentReferencePropertiesEnclosing {

    /**
     * Called after the value of the widget has changed.
     */
    void afterReferencedComponent();

}
