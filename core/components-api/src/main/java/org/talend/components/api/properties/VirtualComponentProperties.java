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
 *  This is used for virtual component
 *  Split the properties for virtual component.
 */

public interface VirtualComponentProperties {

    /**
     *  Get input part component properties
     */
    ComponentProperties getInputComponentProperties();

    /**
     *  Get output part component properties
     */
    ComponentProperties getOutputComponentProperties();

}
