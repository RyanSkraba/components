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

package org.talend.components.adapter.beam;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;

/**
 * Make the TCOMP Sink work on Beam runtime, wrapper it with relate beam interface
 * TCOMP Sink must be serializable
 */
public class TCompSinkAdapter extends PTransform<PCollection<IndexedRecord>, PDone> {

    //TODO(bchen) implement this SinkAdapter by PTransform/ParDo/DoFn
    @Override
    public PDone expand(PCollection<IndexedRecord> input) {
        return null;
    }
}
