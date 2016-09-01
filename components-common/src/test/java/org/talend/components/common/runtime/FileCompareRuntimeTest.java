package org.talend.components.common.runtime;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FileCompareRuntimeTest {

    @Test
    public void testCompareFile() throws IOException {
        String resourceFolder = this.getClass().getResource("/").getPath();
        //Text content and Binary both same
        String srcFile = resourceFolder + "/compare/source.csv";
        String refFile = resourceFolder + "/compare/bin_same_ref.csv";
        Assert.assertTrue(FileCompareRuntime.compareInTextMode(srcFile, refFile, "UTF-8"));
        Assert.assertTrue(FileCompareRuntime.compareInBinaryMode(srcFile, refFile));

        //Text content are same and Binary is different
        srcFile = resourceFolder + "/compare/source.csv";
        refFile = resourceFolder + "/compare/bin_diff_ref.csv";
        Assert.assertTrue(FileCompareRuntime.compareInTextMode(srcFile, refFile, "UTF-8"));
        Assert.assertFalse(FileCompareRuntime.compareInBinaryMode(srcFile, refFile));

        //Text are different
        srcFile = resourceFolder + "/compare/source.csv";
        refFile = resourceFolder + "/compare/text_diff_ref.csv";
        Assert.assertFalse(FileCompareRuntime.compareInTextMode(srcFile, refFile, "UTF-8"));
        Assert.assertFalse(FileCompareRuntime.compareInBinaryMode(srcFile, refFile));
    }
}
