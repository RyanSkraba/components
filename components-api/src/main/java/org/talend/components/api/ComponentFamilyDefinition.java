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
package org.talend.components.api;

import org.talend.daikon.NamedThing;
import org.talend.daikon.definition.Definition;

/**
 * Placeholder for a Component family catalog item.
 */
public interface ComponentFamilyDefinition extends NamedThing {

    Iterable<? extends Definition> getDefinitions();

}
