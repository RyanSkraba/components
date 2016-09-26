package org.talend.components.common.runtime;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileRuntimeHelper {

    /**
     * Compare file with file text content
     * 
     * @param srcFilePath Source file path
     * @param referFilePath Reference file path
     * @param encoding File encoding
     * @return whether those two file are same
     * @throws IOException
     */
    public static boolean compareInTextMode(String srcFilePath, String referFilePath, String encoding) throws IOException {
        BufferedReader srcFile = new BufferedReader(new InputStreamReader(new FileInputStream(srcFilePath), encoding));
        BufferedReader referFile = new BufferedReader(new InputStreamReader(new FileInputStream(referFilePath), encoding));
        String contentSrc = null;
        String contentRefer = null;
        boolean compareResult = true;
        while ((contentSrc = srcFile.readLine()) != null && (contentRefer = referFile.readLine()) != null) {
            if (contentSrc.compareTo(contentRefer) != 0) {
                compareResult = false;
                break;
            }
        }
        // Check if files has a different number of lines
        if (compareResult) {
            if (contentSrc == null) {
                contentRefer = referFile.readLine();
            }
            if (contentSrc != null || contentRefer != null) {
                compareResult = false;
            }
        }

        srcFile.close();
        referFile.close();
        return compareResult;
    }

    /**
     * Compare file with file binary content
     * 
     * @param srcFilePath Source file path
     * @param referFilePath Reference file path
     * @return whether those two file are same
     * @throws IOException
     */
    public static boolean compareInBinaryMode(String srcFilePath, String referFilePath) throws IOException {
        boolean compareResult = true;

        FileChannel srcFileChannel = new FileInputStream(srcFilePath).getChannel();
        long fileLength = srcFileChannel.size();
        srcFileChannel.close();
        FileChannel referFileChannel = new FileInputStream(referFilePath).getChannel();
        long fileRefLength = referFileChannel.size();
        referFileChannel.close();
        if (fileLength != fileRefLength) {
            compareResult = false;
        }
        if (compareResult) {

            BufferedInputStream srcFile = new BufferedInputStream(new FileInputStream(srcFilePath));
            BufferedInputStream referFile = new BufferedInputStream(new FileInputStream(referFilePath));
            int contentSrc = -1;
            int contentRefer = -1;
            while ((contentSrc = srcFile.read()) != -1 && (contentRefer = referFile.read()) != -1) {
                if (contentSrc != contentRefer) {
                    compareResult = false;
                    break;
                }
            }
            srcFile.close();
            referFile.close();

        }
        return compareResult;
    }

    protected static ZipInputStream createZipInputStream(Object source) throws IOException {
        ZipInputStream zipInputStream = null;
        if (source != null) {
            if (source instanceof InputStream) {
                zipInputStream = new ZipInputStream(new BufferedInputStream((InputStream) source));
            } else {
                zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(String.valueOf(source))));
            }
        }
        return zipInputStream;

    }

    /**
     * Generate ZipInputStream with file
     * 
     * @param fileName zip file name
     * @return a new ZIP input stream
     * @throws IOException
     */
    public static ZipInputStream getZipInputStream(String fileName) throws IOException {
        return createZipInputStream(fileName);
    }

    /**
     * Generate ZipInputStream with InputStream
     * 
     * @param stream source file InputStream
     * @return a new ZIP input stream
     * @throws IOException
     */
    public static ZipInputStream getZipInputStream(InputStream stream) throws IOException {
        return createZipInputStream(stream);

    }

    /**
     * Get current zip entry from a specify ZipInputStream instance which is being iterated
     * 
     * @param zipInputStream a ZIP input stream
     * @return current zip entry in specify ZIP input stream
     * @throws IOException
     */
    public static ZipEntry getCurrentZipEntry(ZipInputStream zipInputStream) throws IOException {
        ZipEntry zipEntry = null;
        while (true) {
            zipEntry = zipInputStream.getNextEntry();
            if (zipEntry != null && zipEntry.isDirectory()) {
                continue;
            }
            return zipEntry;
        }
    }
}
