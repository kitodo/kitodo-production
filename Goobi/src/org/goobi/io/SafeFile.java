/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * A {@link java.io.File} delegate class that does not return {@code null} from
 * its {@code list()} functions. It also provides method calls of
 * {@link org.apache.commons.io.FileUtils} for an object oriented usage.
 *
 * @author Matthias Ronge
 */
public class SafeFile implements Comparable<SafeFile> {

    private static final Logger logger = Logger.getLogger(SafeFile.class);

    private static final SafeFile[] NO_FILE = new SafeFile[0];

    private static final String[] NO_STRING = new String[0];

    public static List<SafeFile> createAll(List<File> files) {
        ArrayList<SafeFile> result = new ArrayList<SafeFile>(files.size());
        for (File file : files) {
            result.add(new SafeFile(file));
        }
        return result;
    }

    public static SafeFile createTempFile(String prefix, String suffix) throws IOException {
        return new SafeFile(File.createTempFile(prefix, suffix));
    }

    private final File delegate;

    public SafeFile(File path) {
        delegate = new File(path.getPath());
    }

    public SafeFile(File parent, String child) {
        delegate = new File(parent, child);
    }

    public SafeFile(SafeFile parent, String child) {
        delegate = new File(parent != null ? parent.delegate : null, child);
    }

    public SafeFile(String pathname) {
        delegate = new File(pathname);
    }

    public SafeFile(String parent, String child) {
        delegate = new File(parent, child);
    }

    public SafeFile(URI uri) {
        delegate = new File(uri);
    }

    public boolean canRead() {
        return delegate.canRead();
    }

    @Override
    public int compareTo(SafeFile other) {
        return delegate.compareTo(other.delegate);
    }

    /**
     * Copy directory.
     *
     * @param destDir
     *            the destination directory
     * @throws IOException
     */
    public void copyDir(SafeFile destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        FileUtils.copyDirectory(delegate, destDir.delegate, false);
    }

    public void copyFile(SafeFile destFile) throws IOException {
        FileUtils.copyFile(delegate, destFile.delegate);
    }

    /**
     * Copies src file to dst file. If the dst file does not exist, it is
     * created.
     */
    public void copyFile(SafeFile destFile, boolean preserveFileDate) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("copy " + delegate.getCanonicalPath() + " to " + destFile.delegate.getCanonicalPath());
        }
        FileUtils.copyFile(delegate, destFile.delegate, preserveFileDate);
    }

    public void copyFileToDirectory(SafeFile destDir) throws IOException {
        FileUtils.copyFileToDirectory(delegate, destDir.delegate);
    }

    private SafeFile[] createAll(File[] files) {
        SafeFile[] result = new SafeFile[files.length];
        for (int i = 0; i < files.length; i++)
            result[i] = new SafeFile(files[i]);
        return result;
    }

    public FileInputStream createFileInputStream() throws FileNotFoundException {
        return new FileInputStream(delegate);
    }

    public FileOutputStream createFileOutputStream() throws FileNotFoundException {
        return new FileOutputStream(delegate);
    }

    public FileReader createFileReader() throws FileNotFoundException {
        return new FileReader(delegate);
    }

    public FileWriter createFileWriter() throws IOException {
        return new FileWriter(delegate);
    }

    public boolean createNewFile() throws IOException {
        return delegate.createNewFile();
    }

    public boolean delete() {
        return delegate.delete();
    }

    /**
     * Deletes all files and subdirectories under dir. Returns true if all
     * deletions were successful or if dir does not exist. If a deletion fails,
     * the method stops attempting to delete and returns false.
     */
    public boolean deleteDir() {
        if (!delegate.exists()) {
            return true;
        }
        try {
            FileUtils.deleteDirectory(delegate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteDirectory() throws IOException {
        FileUtils.deleteDirectory(delegate);
    }

    public void deleteQuietly() {
        FileUtils.deleteQuietly(delegate);
    }

    public boolean exists() {
        return delegate.exists();
    }

    public void forceDelete() throws IOException {
        FileUtils.forceDelete(delegate);
    }

    public SafeFile getAbsoluteFile() {
        return new SafeFile(delegate.getAbsolutePath());
    }

    public String getAbsolutePath() {
        return delegate.getAbsolutePath();
    }

    public String getCanonicalPath() throws IOException {
        return delegate.getCanonicalPath();
    }

    public List<File> getCurrentFiles() {
        File[] result = this.delegate.listFiles();
        if (result != null) {
            return Arrays.asList(result);
        } else {
            return Collections.emptyList();
        }
    }

    public List<File> getFilesByFilter(FilenameFilter filter) {
        File[] result = this.delegate.listFiles(filter);
        if (result != null) {
            return Arrays.asList(result);
        } else {
            return Collections.emptyList();
        }
    }

    public String getName() {
        return delegate.getName();
    }

    public SafeFile getParentFile() {
        return new SafeFile(delegate.getParentFile());
    }

    public String getPath() {
        return delegate.getPath();
    }

    public boolean isDirectory() {
        return delegate.isDirectory();
    }

    public boolean isFile() {
        return delegate.isFile();
    }

    public long lastModified() {
        return delegate.lastModified();
    }

    public long length() {
        return delegate.length();
    }

    public String[] list() {
        String[] unchecked = delegate.list();
        return unchecked != null ? unchecked : NO_STRING;
    }

    public String[] list(FilenameFilter filter) {
        String[] unchecked = delegate.list(filter);
        return unchecked != null ? unchecked : NO_STRING;
    }

    public SafeFile[] listFiles() {
        File[] unchecked = delegate.listFiles();
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }

    public SafeFile[] listFiles(FileFilter filter) {
        File[] unchecked = delegate.listFiles(filter);
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }

    public SafeFile[] listFiles(FilenameFilter filter) {
        File[] unchecked = delegate.listFiles(filter);
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }

    public boolean mkdir() {
        return delegate.mkdir();
    }

    public boolean mkdirs() {
        return delegate.mkdirs();
    }

    public void moveDirectory(SafeFile destDir) throws IOException {
        FileUtils.moveDirectory(delegate, destDir.delegate);
    }

    public void moveDirectory(String destDir) throws IOException {
        FileUtils.moveDirectory(delegate, new File(destDir));
    }

    public void moveFile(SafeFile dest) throws IOException {
        FileUtils.moveFile(delegate, dest.delegate);
    }

    public boolean renameTo(SafeFile dest) {
        return delegate.renameTo(dest.delegate);
    }

    public boolean setLastModified(long time) {
        return delegate.setLastModified(time);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public URI toURI() {
        return delegate.toURI();
    }

    @Deprecated
    public URL toURL() throws MalformedURLException {
        return delegate.toURL();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SafeFile other = (SafeFile) obj;
        if (delegate == null) {
            if (other.delegate != null)
                return false;
        } else if (!delegate.equals(other.delegate))
            return false;
        return true;
    }
}
