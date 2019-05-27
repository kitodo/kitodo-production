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
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
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
        MetsService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (MetsService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new MetsService();
                    instance = localReference;
                }
            }
        }
        return localReference;
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
        return loadWorkpiece(uri).getRootElement().getType();
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
        try (InputStream inputStream = ServiceManager.getFileService().read(uri)) {
            logger.info("Reading {}", uri.toString());
            return metsXmlElementAccess.read(inputStream);
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
        try (OutputStream outputStream = ServiceManager.getFileService().write(uri)) {
            logger.info("Saving {}", uri.toString());
            save(workpiece, outputStream);
        }
    }

    public void save(Workpiece workpiece, OutputStream outputStream) throws IOException {
        metsXmlElementAccess.save(workpiece, outputStream);
    }
}
