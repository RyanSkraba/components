package org.talend.components.filedelimited.runtime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/*
 * Implement the function of routines.system.BufferedOutput which we can't be used here
 */
public class BufferedOutput extends BufferedWriter {

    private Writer out;

    private char cb[];

    private int nChars, nextChar;

    public BufferedOutput(Writer out) {
        super(out);
    }

    public BufferedOutput(Writer out, int sz) {
        super(out, sz);
    }

    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed"); //$NON-NLS-1$
    }

    void flushBuffer() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar == 0)
                return;
            out.write(cb, 0, nextChar);

            // Flush the buffer of inside writer.
            out.flush();
            nextChar = 0;
        }
    }

    public void write(String s, int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();

            if (len >= nChars) {
                /*
                 * If the request length exceeds the size of the output buffer, flush the buffer and then write the data
                 * directly. In this way buffered streams will cascade harmlessly.
                 */
                flushBuffer();
                char[] cbuf = new char[len];
                s.getChars(off, (off + len), cbuf, 0);
                write(cbuf, 0, len);
                return;
            }

            if (len > 0) {
                /*
                 * If it doesn't write all char of string to buffer, flush buffer chars then put the String to buffer.
                 * Make sure String doesn't cut by ohter writer.
                 */
                if (nChars - nextChar < len) {
                    flushBuffer();
                }
                s.getChars(off, off + len, cb, nextChar);
                nextChar += len;
                if (nextChar >= nChars)
                    flushBuffer();
            }
        }
    }
}
