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
package org.talend.components.test;

import org.talend.components.api.service.internal.ComponentRegistry;
import org.talend.components.api.service.internal.ComponentServiceImpl;

/**
 * created by sgandon on 14 déc. 2015
 */
public class SimpleComponentService extends ComponentServiceImpl {

    /**
     * DOC sgandon SimpleComponentService constructor comment.
     * 
     * @param componentRegistry
     */
    public SimpleComponentService(ComponentRegistry componentRegistry) {
        super(componentRegistry);
    }

}
