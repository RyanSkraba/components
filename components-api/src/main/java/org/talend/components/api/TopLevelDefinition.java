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
     * This shall get called by the framework once the Component is instanciated. Actually this is not true, it is set
     * once the component is first required by the client through the ComponentService apis
     * 
     * @param globalContext
     */
    // public void setGlobalContext(GlobalContext globalContext);

}