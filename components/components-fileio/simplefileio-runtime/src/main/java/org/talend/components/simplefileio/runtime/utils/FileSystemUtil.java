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

package org.talend.components.simplefileio.runtime.utils;

import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

public class FileSystemUtil {

    /**
     * Return files in this folder, but do not return the hidden file(start with '_' or '.')
     * @param fs
     * @param folder
     */
    public static FileStatus[] listSubFiles(FileSystem fs, String folder) throws IOException {
        return listSubFiles(fs, new Path(folder));
    }

    /**
     * Return files in this folder, but do not return the hidden file(start with '_' or '.')
     * @param fs
     * @param folder
     */
    public static FileStatus[] listSubFiles(FileSystem fs, Path folder) throws IOException {
        return fs.listStatus(folder, new PathFilter() {

            @Override
            public boolean accept(Path path) {
                String name = path.getName();
                return !name.startsWith("_") && !name.startsWith(".");
            }
        });
    }
}
