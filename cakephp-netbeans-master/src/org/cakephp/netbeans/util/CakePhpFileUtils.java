/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.cakephp.netbeans.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public class CakePhpFileUtils {

    /**
     * Unzip specified URL.
     *
     * @param url
     * @param targetDirectory
     * @param filter
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void unzip(String url, File targetDirectory, ZipEntryFilter filter) throws MalformedURLException, IOException {
        if (targetDirectory == null) {
            return;
        }
        URL zipUrl = new URL(url);
        try (ZipInputStream zipInputStream = new ZipInputStream(zipUrl.openStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!filter.accept(entry)) {
                    zipInputStream.closeEntry();
                    continue;
                }
                String path = filter.getPath(entry);
                // display path
                filter.setText(path);

                File outFile = new File(targetDirectory, path);
                createDirectories(outFile, entry);
                writeFile(outFile, zipInputStream, entry);
            }
        }
    }

    /**
     * Write data. If file is directory, do nothing. otherwise, write data.
     *
     * @param outFile
     * @param zipInputStream
     * @param entry
     * @throws IOException
     */
    private static void writeFile(File outFile, ZipInputStream zipInputStream, ZipEntry entry) throws IOException {
        try {
            if (entry.isDirectory()) {
                return;
            }
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile));
            int data;
            try {
                while ((data = zipInputStream.read()) != -1) {
                    outputStream.write(data);
                }
            } finally {
                outputStream.close();
            }
        } finally {
            zipInputStream.closeEntry();
        }
    }

    /**
     * Create directories. In case of file, if parent directory doesn't exist,
     * make parent directories. In case of directory, make directory.
     *
     * @param file
     * @param entry
     */
    private static void createDirectories(File file, ZipEntry entry) {
        if (entry.isDirectory()) {
            file.mkdir();
        } else {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
        }
    }

    /**
     * Change app/tmp directory permission (777)
     *
     * @param tmpDirectory app/tmp Directory
     */
    public static void chmodTmpDirectory(FileObject tmpDirectory) {
        if (tmpDirectory == null) {
            return;
        }
        File tmp = FileUtil.toFile(tmpDirectory);
        tmp.setExecutable(true, false);
        tmp.setReadable(true, false);
        tmp.setWritable(true, false);
        Enumeration<? extends FileObject> children = tmpDirectory.getChildren(true);
        while (children.hasMoreElements()) {
            File child = FileUtil.toFile(children.nextElement());
            child.setExecutable(true, false);
            child.setReadable(true, false);
            child.setWritable(true, false);
        }
    }
}
