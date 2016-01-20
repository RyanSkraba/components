// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.runtime;

import org.apache.avro.generic.IndexedRecord;

/**
 * created by sgandon on 14 janv. 2016
 * 
 * @param <OutputObject>
 */
public interface SingleOutputConnector<OutputObject extends IndexedRecord> {

    public void outputMainData(OutputObject out);
}
