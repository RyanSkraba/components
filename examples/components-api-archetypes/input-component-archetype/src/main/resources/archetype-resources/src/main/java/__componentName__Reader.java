#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

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
package ${package};

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;

/**
 * Simple implementation of a reader.
 */
public class ${componentName}Reader extends AbstractBoundedReader<String> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(${componentName}Definition.class);

    private RuntimeContainer container;

    private final String filename;

    private boolean started = false;

    private BufferedReader reader = null;

    private transient String current;

    public ${componentName}Reader(RuntimeContainer container, BoundedSource source, String filename) {
        super(source);
        this.container = container;
        this.filename = filename;
    }

    @Override
    public boolean start() throws IOException {
        started = true;
        LOGGER.debug("open: " + filename); //$NON-NLS-1$
        reader = new BufferedReader(new FileReader(filename));
        current = reader.readLine();
        return current != null;
    }

    @Override
    public boolean advance() throws IOException {
        current = reader.readLine();
        return current != null;
    }

    @Override
    public String getCurrent() throws NoSuchElementException {
        if (!started) {
            throw new NoSuchElementException();
        }
        return current;
    }

    @Override
    public void close() throws IOException {
        reader.close();
        LOGGER.debug("close: " + filename); //$NON-NLS-1$
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return new Result().toMap();
    }

}
