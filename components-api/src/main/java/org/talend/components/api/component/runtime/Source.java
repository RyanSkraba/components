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

package org.talend.components.api.component.runtime;

import org.talend.components.api.container.RuntimeContainer;

/**
 * Base interface for defining containing input properties and creating a {@link Reader} for reading the input.
 *
 * <p>
 * See {@link SourceOrSink}.
 *
 */
public interface Source extends SourceOrSink {

    /**
     * Returns a new {@link Reader} that reads from this source.
     */
    Reader createReader(RuntimeContainer container);

}
