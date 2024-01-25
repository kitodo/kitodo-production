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

package org.kitodo;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Deletes a subtree on the file system, like Linux’s {@code rm -r} or DOS’s
 * {@code DELTREE}.
 */
public class TreeDeleter extends SimpleFileVisitor<Path> {
    private static final TreeDeleter instance = new TreeDeleter();

    public static void deltree(File file) throws IOException {
        deltree(file.toPath());
    }

    public static void deltree(Path path) throws IOException {
        Files.walkFileTree(path, instance);
    }

    /**
     * Afterwards, delete the directory.
     */
    @Override
    public FileVisitResult postVisitDirectory(Path theDirectory, IOException unused) throws IOException {
        Files.delete(theDirectory);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Delete all files in the directory.
     */
    @Override
    public FileVisitResult visitFile(Path child, BasicFileAttributes unused) throws IOException {
        Files.delete(child);
        return FileVisitResult.CONTINUE;
    }
}
