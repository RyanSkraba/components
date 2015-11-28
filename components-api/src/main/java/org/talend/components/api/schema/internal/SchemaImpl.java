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
package org.talend.components.api.schema.internal;

import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;

import com.cedarsoftware.util.io.JsonWriter;

public class SchemaImpl implements Schema {

    protected SchemaElement root;

    @Override
    public SchemaElement getRoot() {
        return root;
    }

    @Override
    public SchemaElement setRoot(SchemaElement root) {
        this.root = root;
        return root;
    }

    @Override
    public String toSerialized() {
        return JsonWriter.objectToJson(this);
    }
}
