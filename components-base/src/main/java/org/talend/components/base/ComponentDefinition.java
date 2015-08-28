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
package org.talend.components.base;

/**
 * Component definition service.
 * <p>
 * An instance of this class handles the setup of the layoutMap associated with a components.
 *
 * @author Francis Upton
 */

public abstract class ComponentDefinition {

    /**
     * Component categorization - this is an issue that wants further study. - which designer (big data, di, etc) and
     * then which family.
     */
    public enum Family {
        BUSINESS,
        CLOUD
    }

    /*
     * Where do we specify a wizard is required? Maybe list of groups that comprise wizard.
     */

    /*
     * Intercomponent property references - need examples for this. - shared clumps of layoutMap, referring to
     * layoutMap in the same job, refers to layoutMap upstream in the connection.
     * 
     * all layoutMap should support context variables (non-text layoutMap need this).
     */

    public abstract ComponentProperties createProperties();

    public void setDesignerFamily(Family family) {

    }

    public abstract Family[] getSupportedFamilies();

    public abstract String getName();

}
