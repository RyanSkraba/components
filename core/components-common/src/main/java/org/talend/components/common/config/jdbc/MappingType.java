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
package org.talend.components.common.config.jdbc;

import java.util.HashSet;
import java.util.Set;

/**
 * Type mapping between <code>SourceT</code> and <code>TargetT</code>
 * Provides target types to which source type can be mapped. Also provides default target mapping type
 */
public class MappingType<SourceT, TargetT> {

    private final SourceT sourceType;

    private final TargetT defaultTargetType;

    private final Set<TargetT> alternativeTargetTypes;

    public MappingType(SourceT sourceType, TargetT defaultTargetType, Set<TargetT> alternativeTargetTypes) {
        this.sourceType = sourceType;
        this.defaultTargetType = defaultTargetType;
        this.alternativeTargetTypes = new HashSet<>(alternativeTargetTypes);
    }

    public TargetT getDefaultType() {
        return defaultTargetType;
    }

    public Set<TargetT> getAdvisedTypes() {
        return alternativeTargetTypes;
    }

    public SourceT getSourceType() {
        return sourceType;
    }
}
