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

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.NamedThing;

/**
 * Placeholder for a Component family catalog item.
 */

public interface ComponentFamilyDefinition extends NamedThing {

    Iterable<ComponentDefinition> getComponents();

    Iterable<ComponentWizardDefinition> getComponentWizards();
}
