package org.goobi.io;

import java.io.*;
import java.net.URI;
import java.util.*;

public class SafeFile implements Comparable<SafeFile> {
	
	private final File delegate;

    private static final SafeFile[] NO_FILE = new SafeFile[0];
    private static final String[] NO_STRING = new String[0];

    public static SafeFile createTempFile(String prefix, String suffix) throws IOException {
        return new SafeFile(File.createTempFile(prefix, suffix));
    }

    public SafeFile(String pathname) {
    	delegate = new File(pathname);
    }

    public SafeFile(File path) {
    	delegate = new File(path.getPath());
    }

    public SafeFile(URI uri) {
    	delegate = new File(uri);
    }

    public SafeFile(String parent, String child) {
    	delegate = new File(parent, child);
    }

    public SafeFile(File parent, String child) {
    	delegate = new File(parent, child);
    }

    public SafeFile(SafeFile parent, String child) {
    	delegate = new File(parent.delegate, child);
	}

	private SafeFile[] createAll(File[] files) {
        SafeFile[] result = new SafeFile[files.length];
        for (int i = 0; i < files.length; i++)
            result[i] = new SafeFile(files[i]);
        return result;
    }

    public static List<SafeFile> createAll(List<File> files) {
        ArrayList<SafeFile> result = new ArrayList<SafeFile>(files.size());
        for (File file:files){
            result.add(new SafeFile(file));
        }
        return result;
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

    public SafeFile[] listFiles(FilenameFilter filter) {
        File[] unchecked = delegate.listFiles(filter);
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }

    public SafeFile[] listFiles(FileFilter filter) {
        File[] unchecked = delegate.listFiles(filter);
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }

	public boolean isDirectory() {
		return delegate.isDirectory();
	}

	public boolean exists() {
		return delegate.exists();
	}

	public boolean mkdir() {
		return delegate.mkdir();
	}

	public boolean isFile() {
		return delegate.isFile();
	}

	public String getName() {
		return delegate.getName();
	}

	public String getAbsolutePath() {
		return delegate.getAbsolutePath();
	}

	public SafeFile getAbsoluteFile() {
		return new SafeFile(delegate.getAbsolutePath());
	}

	public long lastModified() {
		return delegate.lastModified();
	}

	public File toFile() {
		return delegate;
	}

	public URI toURI() {
		return delegate.toURI();
	}

	public boolean delete() {
		return delegate.delete();
	}

	public boolean renameTo(SafeFile dest) {
		return delegate.renameTo(dest.delegate);
	}

	public boolean canRead() {
		return delegate.canRead();
	}

	public boolean setLastModified(long time) {
		return delegate.setLastModified(time);
	}

	public String getCanonicalPath() throws IOException {
		return delegate.getCanonicalPath();
	}

	public boolean mkdirs() {
		return delegate.mkdirs();
	}

	@Override
	public int compareTo(SafeFile other) {
		return delegate.compareTo(other.delegate);
	}

	public SafeFile getParentFile() {
		return new SafeFile(delegate.getParentFile());
	}

	public String getPath() {
		return delegate.getPath();
	}

	public long length() {
		return delegate.length();
	}

	public List<File> getCurrentFiles() {
		File[] result = this.delegate.listFiles();
		if(result != null){
			return Arrays.asList(result);
		}else{
			return Collections.emptyList();
		}
	}

	public List<File> getFilesByFilter(FilenameFilter filter) {
		File[] result = this.delegate.listFiles(filter);
		if(result != null){
			return Arrays.asList(result);
		}else{
			return Collections.emptyList();
		}
	}

	public boolean createNewFile() throws IOException {
		return delegate.createNewFile();	
	}

}
