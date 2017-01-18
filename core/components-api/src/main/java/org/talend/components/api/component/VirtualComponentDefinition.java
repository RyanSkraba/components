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
package org.talend.components.api.component;

/**
 * A {@code VirtualComponent} is a component which is implemented by two other components, one for input and one for
 * output.
 *
 * <p>
 * The purpose of the {@code VirtualComponent} is simply a convenience to present the two related components that are
 * often used together as a single component. The runtime execution of these components is the same as if the virtual
 * component was not used.
 */
public interface VirtualComponentDefinition {

    /**
     * Return the input component definition of current virtual component.
     *
     */
    ComponentDefinition getInputComponentDefinition();

    /**
     * Return the output component definition of current virtual component.
     *
     */
    ComponentDefinition getOutputComponentDefinition();

}
