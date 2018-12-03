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

package org.kitodo.legacy.joining;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.MetsModsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.dataformat.MetsService;
import org.kitodo.services.file.FileService;

public class MetsModsJoint implements MetsModsInterface {
    private static final Logger logger = LogManager.getLogger(MetsModsJoint.class);

    private final ServiceManager serviceLoader = new ServiceManager();
    private final FileService fileService = serviceLoader.getFileService();
    private final MetsService metsService = serviceLoader.getMetsService();

    private LockResult access;

    private MetsXmlElementAccessInterface workpiece;

    @Override
    public DigitalDocumentInterface getDigitalDocument() throws PreferencesException {
        logger.log(Level.TRACE, "getDigitalDocument()");
        return new DigitalDocumentJoint(workpiece);
    }

    @Override
    public void read(String path) throws ReadException {
        logger.log(Level.TRACE, "read(path: \"{}\")", path);

        try {
            URI uri = new File(path).toURI();

            lockOrElseThrowReadException(uri);
            workpiece = metsService.createMets();
            try (InputStream in = fileService.read(uri, access)) {
                workpiece.read(in);
            }

        } catch (IOException e) {
            throw new ReadException(e.getMessage(), e);
        }
    }

    /**
     * We overload finalize here to return the lock. This is critical because
     * you cannot predict when that will happen (in Tomcat probably very, very
     * soon) and therefore it is important to solve it differently when
     * upgrading the UGH interface. But at the moment there is no better
     * solution.
     */
    @Override
    protected void finalize() throws Throwable {
        if (access != null) {
            try {
                access.close();
            } catch (RuntimeException e) {
                logger.catching(Level.WARN, e);
            }
        }
        super.finalize();
    }

    private void lockOrElseThrowReadException(URI uri) throws ReadException {
        try {
            HashMap<URI, LockingMode> lockenRequest = new HashMap<>(2);
            lockenRequest.put(uri, LockingMode.EXCLUSIVE);
            if (access != null) {
                problemsToReadException(uri, access.tryLock(lockenRequest));
            } else {
                LockResult lockResult = fileService.tryLock(lockenRequest);
                if (lockResult.isSuccessful()) {
                    access = lockResult;
                } else {
                    problemsToReadException(uri, lockResult.getConflicts());
                }
            }
        } catch (IOException e) {
            throw new ReadException(e.getMessage(), e);
        }
    }

    private void problemsToReadException(URI uri, Map<URI, Collection<String>> problems) throws ReadException {
        Collection<String> conflictingUsers = problems.get(uri);
        if (Objects.nonNull(conflictingUsers)) {
            StringBuilder message = new StringBuilder();
            message.append("Cannot lock ");
            message.append(uri);
            message.append(" because it is already locked by ");
            message.append(String.join("; ", conflictingUsers));
            throw new ReadException(message.toString());
        }
    }

    @Override
    public void setDigitalDocument(DigitalDocumentInterface digitalDocument) {
        logger.log(Level.TRACE, "setDigitalDocument(digitalDocument: {})", digitalDocument);
        // TODO Auto-generated method stub
    }

    @Override
    public void write(String filename) throws PreferencesException, WriteException {
        logger.log(Level.TRACE, "write(filename: {})");
        // TODO Auto-generated method stub
    }
}
