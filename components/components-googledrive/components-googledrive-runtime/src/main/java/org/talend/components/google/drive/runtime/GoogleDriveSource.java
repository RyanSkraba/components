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
package org.talend.components.google.drive.runtime;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties;
import org.talend.components.google.drive.create.GoogleDriveCreateProperties;
import org.talend.components.google.drive.delete.GoogleDriveDeleteProperties;
import org.talend.components.google.drive.get.GoogleDriveGetProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

public class GoogleDriveSource extends GoogleDriveSourceOrSink implements BoundedSource {

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResultMutable vr = new ValidationResultMutable(super.validate(container));
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        return validateProperties(properties);
    }

    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        // create action
        if (properties instanceof GoogleDriveCreateProperties) {
            return new GoogleDriveCreateReader(container, this, (GoogleDriveCreateProperties) properties);
        }
        // delete action
        if (properties instanceof GoogleDriveDeleteProperties) {
            return new GoogleDriveDeleteReader(container, this, (GoogleDriveDeleteProperties) properties);
        }
        // list action
        if (properties instanceof GoogleDriveListProperties) {
            return new GoogleDriveListReader(container, this, (GoogleDriveListProperties) properties);
        }
        // get action
        if (properties instanceof GoogleDriveGetProperties) {
            return new GoogleDriveGetReader(container, this, (GoogleDriveGetProperties) properties);
        }
        // put action
        if (properties instanceof GoogleDrivePutProperties) {
            return new GoogleDrivePutReader(container, this, (GoogleDrivePutProperties) properties);
        }
        // copy action
        if (properties instanceof GoogleDriveCopyProperties) {
            return new GoogleDriveCopyReader(container, this, (GoogleDriveCopyProperties) properties);
        }

        return null;
    }
}
