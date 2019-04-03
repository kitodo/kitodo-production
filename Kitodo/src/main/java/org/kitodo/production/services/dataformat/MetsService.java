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

package org.kitodo.production.services.dataformat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class MetsService {
    private static final Logger logger = LogManager.getLogger(MetsService.class);

    private static volatile MetsService instance = null;
    private MetsXmlElementAccessInterface metsXmlElementAccess;

    /**
     * Return singleton variable of type MetsService.
     *
     * @return unique instance of MetsService
     */
    public static MetsService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (MetsService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new MetsService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates a new METS service.
     */
    private MetsService() {
        metsXmlElementAccess = (MetsXmlElementAccessInterface) new KitodoServiceLoader<>(
                MetsXmlElementAccessInterface.class).loadModule();
    }

    /**
     * Function for loading METS files from URI.
     *
     * @param uri
     *            address of the file to be loaded
     * @return loaded file
     * @throws IOException
     *             if the lock can not be requested or if reading is not working
     *             (disk broken, ...)
     */
    public OpenWorkpiece open(URI uri) throws IOException {
        LockResult lockResult = ServiceManager.getFileService().tryLock(uri, LockingMode.EXCLUSIVE);
        if (lockResult.isSuccessful()) {
            try (InputStream inputStream = ServiceManager.getFileService().read(uri, lockResult)) {
                logger.info("Reading {}", uri.toString());
                return new OpenWorkpiece(lockResult, uri, metsXmlElementAccess.read(inputStream));
            }
        } else {
            throw new IOException(createLockErrorMessage(uri, lockResult.getConflicts()));
        }
    }

    public void print(Workpiece workpiece, OutputStream out) throws IOException {
        metsXmlElementAccess.save(workpiece, out);
    }

    /**
     * Function for writing METS file. It is assumed that an exclusive lock
     * exists.
     *
     * @param openWorkpiece
     *            data to be written
     * @throws IOException
     *             if writing does not work (partition full, or is generally not
     *             supported, ...)
     * @throws NullPointerException
     *             if you try to save a new (empty) workpiece that does not have
     *             an URI yet
     */
    public void save(OpenWorkpiece openWorkpiece) throws IOException {
        URI uri = openWorkpiece.getUri();
        try (OutputStream out = ServiceManager.getFileService().write(uri, openWorkpiece.getLockResult())) {
            logger.info("Saving {}", uri.toString());
            metsXmlElementAccess.save(openWorkpiece.getWorkpiece(), out);
        }
    }

    /**
     * The Save As method can save the workpiece under a different address.
     *
     * @param openWorkpiece
     *            data to be written
     * @param uri
     *            address where should be written
     * @throws IOException
     *             if writing does not work (partition full, or is generally not
     *             supported, ...)
     */
    public void saveAs(OpenWorkpiece openWorkpiece, URI uri) throws IOException {
        if (uri.equals(openWorkpiece.getUri())) {
            save(openWorkpiece);
        } else {
            LockResult lockResult = openWorkpiece.getLockResult();
            if (Objects.isNull(lockResult)) {
                lockResult = ServiceManager.getFileService().tryLock(uri, LockingMode.EXCLUSIVE);
                if (!lockResult.isSuccessful()) {
                    throw new IOException(createLockErrorMessage(uri, lockResult.getConflicts()));
                }
                openWorkpiece.setLockResult(lockResult);
            } else {
                // TODO: after upgrading to Java 9, replace with Map.of()
                Map<URI, LockingMode> requests = new HashMap<>(2);
                requests.put(uri, LockingMode.EXCLUSIVE);
                Map<URI, Collection<String>> conflicts = lockResult.tryLock(requests);
                if (!conflicts.isEmpty()) {
                    throw new IOException(createLockErrorMessage(uri, conflicts));
                }
            }
            try (OutputStream out = ServiceManager.getFileService().write(uri, lockResult)) {
                logger.info("Saving {}", uri.toString());
                metsXmlElementAccess.save(openWorkpiece.getWorkpiece(), out);
            }
            lockResult.close(openWorkpiece.getUri());
            openWorkpiece.setUri(uri);
        }
    }

    /**
     * Extracts the formation of the error message as it occurs during both
     * reading and writing. In addition, the error is logged.
     *
     * @param uri
     *            URI to be read/written
     * @param conflicts
     *            reasons why that did not work
     * @return The error message for the exception.
     */
    private String createLockErrorMessage(URI uri, Map<URI, Collection<String>> conflicts) {
        Collection<String> conflictingUsers = conflicts.get(uri);
        StringBuilder buffer = new StringBuilder();
        buffer.append("Cannot lock ");
        buffer.append(uri);
        buffer.append(" because it is already locked by ");
        buffer.append(String.join(" & ", conflictingUsers));
        String message = buffer.toString();
        logger.info(message);
        return message;
    }
}
