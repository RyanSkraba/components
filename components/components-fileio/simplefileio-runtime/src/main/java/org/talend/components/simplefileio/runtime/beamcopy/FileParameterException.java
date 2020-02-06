// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.simplefileio.runtime.beamcopy;

/**
 * Exception when parameters of input file are obviously wrong.
 * For example, wrong 'line separator' that generate "out of memory" exception.
 */
public class FileParameterException extends RuntimeException {

    public FileParameterException(String message) {
        super(message);
    }

    public FileParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
