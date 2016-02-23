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
package org.talend.components.api.component.runtime;

import java.io.Serializable;

public class WriterResult implements Serializable {

    private static final long serialVersionUID = 8670579213592463768L;

    protected String uId;

    /**
     * Getter for uId.
     * 
     * @return the uId
     */
    public String getuId() {
        return this.uId;
    }

    /**
     * Getter for type.
     * 
     * @return the type
     */
    public Type getType() {
        return this.type;
    }

    private Type type;

    public static enum Type {
                             OK,
                             FAILED;
    }

    public WriterResult(String uId, Type type) {
        this.uId = uId;
        this.type = type;
    }

}
