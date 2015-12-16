/*
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2015 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package org.goobi.io;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * A {@link java.io.File} delegate class that does not return {@code null} from
 * its {@code list()} functions. It also provides method calls of
 * {@link org.apache.commons.io.FileUtils} for an object oriented usage.
 * 
 * @author Matthias Ronge
 */
public class SafeFile implements Comparable<SafeFile> {

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
		delegate = new File(parent.delegate, child);
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

	public void copyFile(SafeFile destFile) throws IOException {
		FileUtils.copyFile(delegate, destFile.delegate);
	}

	public void copyFile(SafeFile destFile, boolean preserveFileDate) throws IOException {
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

	public URI toURI() {
		return delegate.toURI();
	}

	@Deprecated
    public URL toURL() throws MalformedURLException {
        return delegate.toURL();
    }
}
