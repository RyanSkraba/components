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
package org.talend.components.filterrow.processing;

/**
 * created by dmytro.chmyga on Nov 22, 2016
 */
public class OrValueProcessor extends ValueProcessor {

    public OrValueProcessor() {
        super(false, true);
    }

    @Override
    protected boolean processResult(boolean result, boolean processForColumn) {
        return result || processForColumn;
    }

}
