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

package org.talend.components.kafka.runtime;

import org.apache.beam.sdk.transforms.DoFn;

import java.nio.charset.Charset;

public class ExtractCsvSplit extends DoFn<byte[], String[]> {

    static {
        // Ensure that the singleton for the KafkaAvroRegistry is created.
        KafkaAvroRegistry.get();
    }

    private final String fieldDelimiter;

    ExtractCsvSplit(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {
        String record = new String(c.element(), Charset.forName("UTF-8"));
        c.output(record.split(fieldDelimiter));
    }

}
