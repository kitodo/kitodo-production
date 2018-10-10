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

package org.kitodo.filemanagement.locking;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The immutable read file management manages the temporary files that need to
 * be created for immutable reading and later deleted. The immutable read file
 * management belongs to the lock management and should only be instantiated by
 * the lock management. Basically, you could have programmed everything together
 * into a class, but then it would have become very large, hence the division.
 */
class ImmutableReadFileManagement {
    private static final Logger logger = LogManager.getLogger(ImmutableReadFileManagement.class);

    /**
     * Map of the up-to-date copies.
     */
    private final Map<URI, URI> upToDateCopies = new HashMap<>();

    /**
     * Which copy of a URI was passed to which user.
     */
    private final Map<URI, UserMapForURI> urisGivenToUsers = new HashMap<>();

    /**
     * Checks if the copy in question can be disposed of, and if so, does it. A
     * copy can be discarded if it isn’t noted by any user more than his or her
     * visible version of this file. To find this out, the entire map must be
     * searched below the original URI. This process can be parallelized very
     * well. If the copy can be disposed of, it must first be removed from the
     * map of up-to-date copies so that it will not be reissued during the
     * deletion, and then deleted. If the file cannot be deleted, a warning is
     * written to the logfile. System administrators should be on the lookout
     * for such alerts, because if they occur frequently, this could signal that
     * the partiton is filling up with temporary files. This can be a problem
     * especially on Windows, because the Java virtual machine occasionally has
     * problems deleting files here.
     *
     * @param copyInQuestion
     *            URI of the file that may be deleted
     * @param originUri
     *            URI of the file of which the file in question is a copy. Since
     *            the maps storing the temporary file information use the URI of
     *            the source file as a key, they are much more efficient to use
     *            if it is known. (It would work without, but then you would
     *            have to chew through the whole map every time.)
     */
    private void cleanUp(URI copyInQuestion, URI originUri) {
        UserMapForURI userMapForURI = urisGivenToUsers.get(originUri);
        if (Objects.isNull(userMapForURI) || userMapForURI.entrySet().parallelStream()
                .map(userMapForUriEntry -> userMapForUriEntry.getValue().getKey())
                .noneMatch(readCopy -> readCopy.equals(copyInQuestion))) {

            URI upToDateCopy = upToDateCopies.get(originUri);
            if (Objects.nonNull(upToDateCopy) && upToDateCopy.equals(copyInQuestion)) {
                upToDateCopies.remove(originUri);
            }

            File fileToDelete = new File(copyInQuestion.getPath());
            if (FileUtils.deleteQuietly(fileToDelete)) {
                logger.debug("the temporary read copy {} was deleted", fileToDelete);
            } else {
                logger.warn("The temporary read file {} could not be deleted.", fileToDelete);
            }
        }
    }

    /**
     * Creates a read copy of a file. The copy will be created in the same
     * directory and marked as a temporary file. This method is called if there
     * is no or no up-to-date copy of the file, but one has been requested.
     *
     * @param uri
     *            file to be copied
     * @return URI of the copy
     * @throws IOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    private URI createReadCopy(URI uri) throws IOException {
        File srcFile = new File(uri.getPath());
        File destFile = deriveTempFile(srcFile);
        FileUtils.copyFile(srcFile, destFile);
        logger.debug("{} was created as a temporary reading copy of {}", destFile.getName(), srcFile);
        return destFile.toURI();
    }

    /**
     * Derives the name of the temporary file from the name of the original file
     * and creates an empty placeholder file with the derived file name in the
     * same directory. The name of the temporary file is formed depending on the
     * operating system so that the file can easily be recognized as a temporary
     * file. For example, {@code PPN012345678.xml} could become
     * {@code PPN012345678.xml-2448103947405693446~}, but
     * {@code PPN012345678_xml-2448103947405693446.tmp} on Windows. The sequence
     * of numbers is inserted by the Java runtime and chosen so that it does not
     * exist guaranteed.
     *
     * @param srcFile
     *            the original file
     * @return the derived file
     * @throws IOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    private File deriveTempFile(File srcFile) throws IOException {
        String prefix = srcFile.getName();
        if (SystemUtils.IS_OS_WINDOWS) {
            prefix = prefix.replace('.', '_');
        }
        prefix = prefix.concat("-");
        String suffix = SystemUtils.IS_OS_WINDOWS ? ".tmp" : "~";
        File directory = srcFile.getParentFile();
        return File.createTempFile(prefix, suffix, directory);
    }

    /**
     * Returns the immutable read copy for a user and a URI. If necessary, the
     * file must be created at this point.
     *
     * @param user
     *            user for whom the immutable read copy is to be returned
     * @param uri
     *            URI for which the immutable read copy is to be returned
     * @return the immutable read copy
     * @throws IOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    URI getImmutableReadCopy(String user, URI uri) throws IOException {
        urisGivenToUsers.computeIfAbsent(uri, create -> new UserMapForURI());
        UserMapForURI userMapForURI = urisGivenToUsers.get(uri);
        if (userMapForURI.containsKey(user)) {
            Pair<URI, AtomicInteger> uriWithCount = userMapForURI.get(user);
            uriWithCount.getValue().incrementAndGet();
            return uriWithCount.getKey();
        } else {
            URI uriOfImmutableReadCopy = getUpToDateCopy(uri);
            userMapForURI.put(user, Pair.of(uriOfImmutableReadCopy, new AtomicInteger(1)));
            return uriOfImmutableReadCopy;
        }
    }

    /**
     * Returns the URI of a up-to-date read copy of the requested URI. Either,
     * there is already one, or a copy process is triggered and the newly
     * created copy is noted as up-to-date.
     *
     * @param originUri
     *            URI for which the immutable read copy is to be returned
     * @return the immutable read copy
     * @throws IOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    private URI getUpToDateCopy(URI originUri) throws IOException {
        URI upToDateCopy = upToDateCopies.get(originUri);
        if (upToDateCopy == null) {
            upToDateCopy = createReadCopy(originUri);
            upToDateCopies.put(originUri, upToDateCopy);
        }
        return upToDateCopy;
    }

    /**
     * Returns whether there is already a read copy of this file.
     *
     * @param originUri
     *            URI for which the immutable read copy is searched
     * @return true, if there is an up-to-date copy of that URI
     */
    boolean isHavingACopyOf(URI originUri) {
        return upToDateCopies.containsKey(originUri);
    }

    /**
     * Marks a URI as changed. If another user subsequently requests a read copy
     * of this URI, a new copy is created for him.
     *
     * @param uri
     *            URI of the file that has changed
     */
    void markAsChanged(URI uri) {
        upToDateCopies.remove(uri);
    }

    /**
     * Removes a reference to the use of a file as a temporary copy. If no
     * reference is left, the user is logged out of the temporary copy. Then it
     * will also be checked if the copy can be deleted.
     *
     * @param originUri
     *            URI to which the user requested the immutable read lock, that
     *            is, the URI of the original file
     * @param user
     *            user who held the lock
     */
    void maybeRemoveReadFile(URI originUri, String user) {
        UserMapForURI userMapForURI = urisGivenToUsers.get(originUri);
        if (Objects.nonNull(userMapForURI)) {
            Pair<URI, AtomicInteger> copyUriWithCount = userMapForURI.get(user);
            if (copyUriWithCount.getValue().decrementAndGet() == 0) {
                userMapForURI.remove(user);
                if (userMapForURI.isEmpty()) {
                    urisGivenToUsers.remove(originUri);
                }
                cleanUp(copyUriWithCount.getKey(), originUri);
            }
        }
    }
}
