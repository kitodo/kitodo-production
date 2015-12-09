package org.goobi.io;

import java.io.*;
import java.net.URI;
import java.util.*;

public class SafeFile extends File {

    private static final SafeFile[] NO_FILE = new SafeFile[0];
    private static final String[] NO_STRING = new String[0];

    public static SafeFile createTempFile(String prefix, String suffix) throws IOException {
        return new SafeFile(File.createTempFile(prefix, suffix));
    }

    public SafeFile(String pathname) {
        super(pathname);
    }

    public SafeFile(File path) {
        super(path.getPath());
    }

    public SafeFile(URI uri) {
        super(uri);
    }

    public SafeFile(String parent, String child) {
        super(parent, child);
    }

    public SafeFile(File parent, String child) {
        super(parent, child);
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

    @Override
    public String[] list() {
        String[] unchecked = super.list();
        return unchecked != null ? unchecked : NO_STRING;
    }

    @Override
    public String[] list(FilenameFilter filter) {
        String[] unchecked = super.list(filter);
        return unchecked != null ? unchecked : NO_STRING;
    }

    @Override
    public SafeFile[] listFiles() {
        File[] unchecked = super.listFiles();
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }

    @Override
    public SafeFile[] listFiles(FilenameFilter filter) {
        File[] unchecked = super.listFiles(filter);
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }

    @Override
    public SafeFile[] listFiles(FileFilter filter) {
        File[] unchecked = super.listFiles(filter);
        return unchecked != null ? createAll(unchecked) : NO_FILE;
    }
}
