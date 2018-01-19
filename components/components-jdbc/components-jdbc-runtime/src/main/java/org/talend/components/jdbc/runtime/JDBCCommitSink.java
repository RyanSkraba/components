package org.talend.components.jdbc.runtime;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;

public class JDBCCommitSink extends JDBCCommitSourceOrSink implements Sink {

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new JDBCCommitWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResultMutable vr = new ValidationResultMutable();
        return vr;
    }

}
