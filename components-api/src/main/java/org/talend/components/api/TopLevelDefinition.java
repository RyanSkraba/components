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
package org.talend.components.api;

/**
 * This interface is used by top level element that need to be presented to a user with a name a displayname and an
 * image
 *
 */
public interface TopLevelDefinition extends NamedThing {

    /**
     * This shall be a path relative to the current Wizard definition, ideally is should just be the name of the png
     * image if placed in the same resouce folder that the current class. This icon will be computed with the following
     * code
     * 
     * <pre>
     * {@code
     *    this.getClass().getResourceAsStream(getIconPngPath())
     * }
     * </pre>
     * 
     * @see {@link java.lang.Class#getResourceAsStream(String)}
     * @return the path to the png resource
     */

    public abstract String getPngImagePath();

}