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
package org.talend.components.processing.runtime.window;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Sessions;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.window.WindowProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

public class WindowRuntime extends PTransform<PCollection<IndexedRecord>, PCollection<IndexedRecord>>
        implements RuntimableRuntime {

    transient private WindowProperties properties;

    @Override
    public PCollection<IndexedRecord> expand(PCollection<IndexedRecord> indexedRecordPCollection) {
        PCollection<IndexedRecord> windowed_items;

        if (properties.windowLength.getValue() < 1) {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).setAndThrow(properties.windowLength.getName(),
                    String.valueOf(properties.windowLength.getValue()));
        }

        // Session Window
        if (properties.windowSession.getValue()) {
            windowed_items = indexedRecordPCollection.apply(Window.<IndexedRecord> into(
                    Sessions.withGapDuration(Duration.millis(properties.windowLength.getValue().intValue()))));
            return windowed_items;
        }

        if (properties.windowSlideLength.getValue() < 1) {
            // Fixed Window
            windowed_items = indexedRecordPCollection.apply(
                    Window.<IndexedRecord> into(FixedWindows.of(new Duration(properties.windowLength.getValue().intValue()))));
        } else {
            // Sliding Window
            windowed_items = indexedRecordPCollection.apply(
                    Window.<IndexedRecord> into(SlidingWindows.of(new Duration(properties.windowLength.getValue().intValue()))
                            .every(new Duration(properties.windowSlideLength.getValue().intValue()))));
        }
        return windowed_items;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, Properties properties) {
        this.properties = (WindowProperties) properties;
        return ValidationResult.OK;
    }
}
