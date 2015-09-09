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

import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.Property;

/**
 * Superclass for {@link Property} and {@link PresentationItem}.
 */
public class AbstractNamedThing implements NamedThing {

    private String name;

    private String displayName;

    public AbstractNamedThing() {
    }

    public AbstractNamedThing(String name, String displayName) {
        this();
        this.name = name;
        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

}
