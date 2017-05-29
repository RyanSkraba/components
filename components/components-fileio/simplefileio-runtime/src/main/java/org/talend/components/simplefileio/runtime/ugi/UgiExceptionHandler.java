package org.talend.components.simplefileio.runtime.ugi;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.mapreduce.lib.input.InvalidInputException;
import org.apache.hadoop.security.AccessControlException;
import org.talend.components.simplefileio.SimpleFileIOErrorCode;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * Wraps a {@link UgiDoAs} to convert any exceptions that may be encountered during the actions execution into
 * {@link org.talend.daikon.exception.TalendRuntimeException}.
 */
public class UgiExceptionHandler extends UgiDoAs {

    public enum AccessType {
        Read,
        Write
    }

    private final UgiDoAs wrapped;

    private final AccessType accessType;

    private final String username;

    private final String path;

    public UgiExceptionHandler(UgiDoAs wrapped, AccessType type, String username, String path) {
        this.wrapped = wrapped;
        this.accessType = type;
        this.username = username;
        this.path = path;
    }

    @Override
    public <T> T doAs(PrivilegedExceptionAction<T> action) throws Exception {
        try {
            return wrapped.doAs(action);
        } catch (AccessControlException e) {
            throw createFrom(e);
        } catch (InvalidInputException e) {
            throw createFrom(e);
        } catch (LoginException e) {
            throw createFrom(e);
        } catch (IOException e) {
            Exception ex = createFromOrNull(e);
            if (ex != null)
                throw ex;
            throw e;
        } catch (RuntimeException e) {
            throw adapt(e);
        }
    }

    @Override
    public <T> T doAs(PrivilegedAction<T> action) {
        try {
            return wrapped.doAs(action);
        } catch (RuntimeException e) {
            // This is typically an UndeclaredThrowableException or
            // PipelineExecutionException.
            throw adapt(e);
        }
    }

    private RuntimeException adapt(RuntimeException e) {
        if (e.getCause() instanceof AccessControlException)
            return createFrom((AccessControlException) e.getCause());
        if (e.getCause() instanceof LoginException)
            return createFrom((LoginException) e.getCause());
        if (e.getCause() instanceof InvalidInputException)
            return createFrom((InvalidInputException) e.getCause());
        if (e.getCause() instanceof IOException) {
            RuntimeException ex = createFromOrNull((IOException) e.getCause());
            if (ex != null)
                return ex;
        }
        return e;
    }

    private TalendRuntimeException createFrom(AccessControlException cause) {
        if (accessType == AccessType.Read)
            return SimpleFileIOErrorCode.createInputNotAuthorized(cause, username, path);
        return SimpleFileIOErrorCode.createOutputNotAuthorized(cause, username, path);
    }

    private TalendRuntimeException createFrom(LoginException cause) {
        // TODO: more specific method for keytab login errors.
        if (accessType == AccessType.Read)
            return SimpleFileIOErrorCode.createInputNotAuthorized(cause, username, path);
        return SimpleFileIOErrorCode.createOutputNotAuthorized(cause, username, path);
    }

    private TalendRuntimeException createFrom(InvalidInputException cause) {
        // TODO: more specific method for file not found errors.
        if (accessType == AccessType.Read)
            return SimpleFileIOErrorCode.createInputNotAuthorized(cause, username, path);
        return SimpleFileIOErrorCode.createOutputNotAuthorized(cause, username, path);
    }

    private TalendRuntimeException createFromOrNull(IOException cause) {
        if (cause.getMessage().startsWith("Mkdirs failed to create ") && cause.getMessage().contains("(exists=false"))
            throw SimpleFileIOErrorCode.createOutputNotAuthorized(cause, username, path);
        return null;
    }
}
