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
 * created by dmytro.chmyga on Dec 23, 2016
 */
public class ProcessingResult {

    private final boolean match;

    private final String errorMessage;

    public ProcessingResult(boolean match, String errorMessage) {
        this.match = match;
        this.errorMessage = errorMessage;
    }

    public boolean isMatch() {
        return match;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
