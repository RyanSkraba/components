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
 * Something that has a name.
 *
 */
public interface NamedThing {

    /**
     * This is a technical name that shall be unique to identify the thing
     * 
     * @return a technical name
     */
    String getName();

    /**
     * This is the name that will be displayed to the user, this may be internationalized.
     * 
     * @return the name to be displayed to the user.
     */
    String getDisplayName();

    /**
     * A multiword title that describes the thing.
     *
     * @return the title
     */
    String getTitle();

}