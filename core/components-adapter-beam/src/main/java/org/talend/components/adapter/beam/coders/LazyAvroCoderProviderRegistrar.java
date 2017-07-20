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

package org.talend.components.adapter.beam.coders;

import java.util.List;

import org.apache.beam.sdk.coders.CoderProvider;
import org.apache.beam.sdk.coders.CoderProviderRegistrar;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;

/**
 * A {@link CoderProviderRegistrar} for {@link LazyAvroCoder}.
 */
@AutoService(CoderProviderRegistrar.class)
public class LazyAvroCoderProviderRegistrar implements CoderProviderRegistrar {

    @Override
    public List<CoderProvider> getCoderProviders() {
        return ImmutableList.of(LazyAvroCoder.getCoderProvider());
    }
}
