package org.talend.components.s3.runtime;

import org.talend.components.api.component.runtime.Sink;

public class S3Sink extends S3SourceOrSink implements Sink {

    /**
     * 
     */
    private static final long serialVersionUID = 7988280326378357785L;

    @Override
    public S3WriteOperation createWriteOperation() {
        return new S3WriteOperation(this);
    }

}
