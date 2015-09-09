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
package org.talend.components.api.exception;

import java.io.IOException;

/**
 * created by sgandon on 9 sept. 2015 Detailled comment
 *
 */
public class ComponentException extends RuntimeException {

    /**
     * DOC sgandon ComponentException constructor comment.
     * 
     * @param string
     */
    public ComponentException(String string) {
        super(string);
    }

    /**
     * DOC sgandon ComponentException constructor comment.
     * 
     * @param e
     */
    public ComponentException(Exception e) {
        super(e);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -84662653622272070L;
    // FIXME this should be similar to dataprep Exception, be will be gathered into a common back-end project
}
