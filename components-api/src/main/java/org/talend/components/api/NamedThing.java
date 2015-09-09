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
 * created by sgandon on 9 sept. 2015 Detailled comment
 *
 */
public interface NamedThing {

    /**
     * This is a techinical name that shall be unique to identify the thing
     * 
     * @return a technical name
     */
    String getName();

    /**
     * This is the name that will be displayed to the user, this may be internationnalized.
     * 
     * @return the name to be displayed to the user.
     */
    String getDisplayName();

}