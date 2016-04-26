package org.talend.components.dataprep;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;

/**
 * Simple implementation of a reader.
 */
public class TDataSetInputReader extends AbstractBoundedReader<String> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TDataSetInputDefinition.class);

    private final String filename;

    private boolean started = false;

    private BufferedReader reader = null;

    private transient String current;

    public TDataSetInputReader(RuntimeContainer container, BoundedSource source, String filename) {
        super(container, source);
        this.filename = filename;
    }

    @Override
    public boolean start() throws IOException {
        return false;
    }

    @Override
    public boolean advance() throws IOException {
        return false;
    }

    @Override
    public String getCurrent() throws NoSuchElementException {
        return "";
    }

    @Override
    public void close() throws IOException {
    }
}
