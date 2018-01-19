// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.query.generator;

import org.talend.components.jdbc.query.EDatabaseTypeName;

public class HiveQueryGenerator extends DefaultQueryGenerator {

    public HiveQueryGenerator(EDatabaseTypeName dbType) {
        super(dbType);
    }

    protected boolean needTextFence4SQL() {
        return false;
    }

    protected boolean needFullName4Column() {
        return false;
    }
}
