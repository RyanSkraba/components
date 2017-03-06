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

package org.talend.components.pubsub.runtime;

import java.util.Set;

import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.pubsub.PubSubDatasetProperties;

public interface IPubSubDatasetRuntime extends DatasetRuntime<PubSubDatasetProperties> {

    public Set<String> listTopics() throws Exception;

    public Set<String> listSubscriptions() throws Exception;
}
