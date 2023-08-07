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

package test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.FileVisitResult.*;

/**
 * Deletes directory structures and their contents.
 */
public class TreeDeleter extends SimpleFileVisitor<Path> {
    /**
     * Deletes a directory with its contents.
     * 
     * @param dir
     *            directory to delete
     */
    public static void delete(Path dir) throws IOException {
        assert Files.notExists(dir) || Files.isDirectory(dir) : "'dir' must be a directory";

        if (Files.exists(dir)) {
            Files.walkFileTree(dir, new TreeDeleter());
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return CONTINUE;
    }
}
