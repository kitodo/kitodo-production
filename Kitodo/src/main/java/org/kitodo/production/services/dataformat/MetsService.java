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

    private MetsService() {
        metsXmlElementAccess = (MetsXmlElementAccessInterface) new KitodoServiceLoader<>(
                MetsXmlElementAccessInterface.class).loadModule();
    }

    /**
     * Returns the type of the top element of the root element, and thus the
     * type of the workpiece.
     *
     * @param uri
     *            Address of the METS file of the workpiece
     * @return the type of root element of the root element of the workpiece
     * @throws IOException
     *             if the file cannot be read (for example, because the file was
     *             not found)
     */
    public String getBaseType(URI uri) throws IOException {
        return loadWorkpiece(uri, LockingMode.IMMUTABLE_READ).getRootElement().getType();
    }

    /**
     * Function for loading METS files from URI.
     *
     * @param uri
     *            address of the file to be loaded
     * @return loaded file
     * @throws IOException
     *             if reading is not working (disk broken, ...)
     */
    public Workpiece loadWorkpiece(URI uri) throws IOException {
        return loadWorkpiece(uri, LockingMode.EXCLUSIVE);
    }

    /**
     * Function for loading METS files from URI.
     *
     * @param uri
     *            address of the file to be loaded
     * @param lockingMode
     *            how to lock the METS file
     * @return loaded file
     * @throws IOException
     *             if reading is not working (disk broken, ...)
     */
    public Workpiece loadWorkpiece(URI uri, LockingMode lockingMode) throws IOException {
        try (LockResult lockResult = ServiceManager.getFileService().tryLock(uri, lockingMode)) {
            if (lockResult.isSuccessful()) {
                try (InputStream inputStream = ServiceManager.getFileService().read(uri, lockResult)) {
                    logger.info("Reading {}", uri.toString());
                    return metsXmlElementAccess.read(inputStream);
                }
            } else {
                throw new IOException(createLockErrorMessage(uri, lockResult));
            }
        }
    }

    /**
     * Function for writing METS files to URI. (URI target must allow writing
     * operation.)
     *
     * @param workpiece
     *            data to be written
     * @param uri
     *            address where should be written
     * @throws IOException
     *             if writing does not work (partition full, or is generally not
     *             supported, ...)
     */
    public void saveWorkpiece(Workpiece workpiece, URI uri) throws IOException {
        try (LockResult lockResult = ServiceManager.getFileService().tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (OutputStream outputStream = ServiceManager.getFileService().write(uri, lockResult)) {
                    logger.info("Saving {}", uri.toString());
                    save(workpiece, outputStream);
                }
            } else {
                throw new IOException(createLockErrorMessage(uri, lockResult));
            }
        }
    }

    public void save(Workpiece workpiece, OutputStream outputStream) throws IOException {
        metsXmlElementAccess.save(workpiece, outputStream);
    }

    /**
     * Extracts the formation of the error message as it occurs during both
     * reading and writing. In addition, the error is logged.
     *
     * @param uri
     *            URI to be read/written
     * @param lockResult
     *            Lock result that did not work
     * @return The error message for the exception.
     */
    private String createLockErrorMessage(URI uri, LockResult lockResult) {
        Collection<String> conflictingUsers = lockResult.getConflicts().get(uri);
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
