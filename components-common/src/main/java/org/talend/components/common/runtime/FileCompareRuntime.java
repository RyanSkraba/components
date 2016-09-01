package org.talend.components.common.runtime;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

public class FileCompareRuntime {

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
}
