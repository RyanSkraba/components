package org.talend.components.common.runtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRuntimeHelperTest {

    private transient static final Logger LOG = LoggerFactory.getLogger(FileRuntimeHelperTest.class);

    @Test
    public void testCompareFile() throws IOException {
        String resources = getClass().getResource("/compare").getPath();
        LOG.info("Resources folder: " + resources);
        // Text content and Binary both same
        String srcFile = resources + "/source.csv";
        String refFile = resources + "/bin_same_ref.csv";
        Assert.assertTrue(FileRuntimeHelper.compareInTextMode(srcFile, refFile, "UTF-8"));
        Assert.assertTrue(FileRuntimeHelper.compareInBinaryMode(srcFile, refFile));

        // Text content are same and Binary is different
        srcFile = resources + "/source.csv";
        refFile = resources + "/bin_diff_ref.csv";
        Assert.assertTrue(FileRuntimeHelper.compareInTextMode(srcFile, refFile, "UTF-8"));
        Assert.assertFalse(FileRuntimeHelper.compareInBinaryMode(srcFile, refFile));

        // Text are different
        srcFile = resources + "/source.csv";
        refFile = resources + "/text_diff_ref.csv";
        Assert.assertFalse(FileRuntimeHelper.compareInTextMode(srcFile, refFile, "UTF-8"));
        Assert.assertFalse(FileRuntimeHelper.compareInBinaryMode(srcFile, refFile));
    }

    @Test
    public void testFileUncompress() throws Throwable {
        String resources = getClass().getResource("/zip").getPath();
        LOG.info("Resources folder: " + resources);
        String zipFile = resources + "/test_uncompress.zip";
        ZipInputStream zipInputStream = FileRuntimeHelper.getZipInputStream(zipFile);
        int entryCount = 0;
        List<String> entries = Arrays.asList("folder_1/file_1_1.txt", "folder_1/file_1_2.csv", "folder_2/file_2_1.txt",
                "folder_2/file_2_2.csv", "test.txt");
        while (true) {
            ZipEntry entry = FileRuntimeHelper.getCurrentZipEntry(zipInputStream);
            if (entry == null) {
                break;
            }
            entryCount++;
            LOG.debug("Entry name: " + entry.getName());
            Assert.assertTrue(entries.contains(entry.getName()));
        }
        LOG.debug("Entries count: " + entryCount);
        Assert.assertEquals(5, entryCount);
    }
}
