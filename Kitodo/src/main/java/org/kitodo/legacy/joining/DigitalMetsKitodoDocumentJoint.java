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
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.FileSetInterface;
import org.kitodo.api.ugh.MetsModsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.dataformat.MetsService;
import org.kitodo.services.file.FileService;

public class DigitalMetsKitodoDocumentJoint implements DigitalDocumentInterface, MetsModsInterface {
    private static final Logger logger = LogManager.getLogger(DigitalMetsKitodoDocumentJoint.class);

    private final ServiceManager serviceLoader = new ServiceManager();
    private final MetsService metsService = serviceLoader.getMetsService();
    private final FileService fileService = serviceLoader.getFileService();

    private MetsXmlElementAccessInterface workpiece = metsService.createMets();

    public DigitalMetsKitodoDocumentJoint() {
        this.workpiece = metsService.createMets();
    }

    DigitalMetsKitodoDocumentJoint(MetsXmlElementAccessInterface workpiece) {
        this.workpiece = workpiece;
    }

    @Override
    public void addAllContentFiles() {
        logger.log(Level.TRACE, "addAllContentFiles()");
        // TODO Auto-generated method stub
    }

    @Override
    public DocStructInterface createDocStruct(DocStructTypeInterface docStructType) {
        logger.log(Level.TRACE, "createDocStruct(docStructType: {})", docStructType);
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
    }

    @Override
    public DigitalDocumentInterface getDigitalDocument() throws PreferencesException {
        logger.log(Level.TRACE, "getDigitalDocument()");
        return new DigitalMetsKitodoDocumentJoint(workpiece);
    }

    @Override
    public FileSetInterface getFileSet() {
        logger.log(Level.TRACE, "getFileSet()");
        // TODO Auto-generated method stub
        return new FileSetJoint();
    }

    @Override
    public DocStructInterface getLogicalDocStruct() {
        logger.log(Level.TRACE, "getLogicalDocStruct()");
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint(workpiece.getStructMap());
    }

    @Override
    public DocStructInterface getPhysicalDocStruct() {
        logger.log(Level.TRACE, "getPhysicalDocStruct()");
        // TODO Auto-generated method stub
        return new PhysicalDocStructJoint();
    }

    @Override
    public void overrideContentFiles(List<String> images) {
        logger.log(Level.TRACE, "overrideContentFiles(images: {})", images);
        // TODO Auto-generated method stub
    }

    @Override
    public void read(String path) throws ReadException {
        logger.log(Level.TRACE, "read(path: \"{}\")", path);
        URI uri = new File(path).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (InputStream in = fileService.read(uri, lockResult)) {
                    logger.info("Reading METS/Kitodo file {}", uri.toString());
                    workpiece.read(in);
                }
            } else {
                Collection<String> conflictingUsers = lockResult.getConflicts().get(uri);
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Cannot lock ");
                messageBuilder.append(uri);
                messageBuilder.append(" because it is already locked by ");
                messageBuilder.append(String.join("; ", conflictingUsers));
                String message = messageBuilder.toString();
                logger.info(message);
                throw new ReadException(message);
            }
        } catch (IOException e) {
            throw new ReadException(e.getMessage(), e);
        }
    }

    @Override
    public void setDigitalDocument(DigitalDocumentInterface digitalDocument) {
        this.workpiece = ((DigitalMetsKitodoDocumentJoint) digitalDocument).workpiece;
    }

    @Override
    public void setLogicalDocStruct(DocStructInterface docStruct) {
        logger.log(Level.TRACE, "setLogicalDocStruct(docStruct: {})", docStruct);
        // TODO Auto-generated method stub
    }

    @Override
    public void setPhysicalDocStruct(DocStructInterface docStruct) {
        logger.log(Level.TRACE, "setPhysicalDocStruct(docStruct: {})", docStruct);
        // TODO Auto-generated method stub
    }

    @Override
    public void write(String filename) throws PreferencesException, WriteException {
        URI uri = new File(filename).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (OutputStream out = fileService.write(uri, lockResult)) {
                    logger.info("Saving METS/Kitodo file {}", uri.toString());
                    workpiece.save(out);
                }
            } else {
                Collection<String> conflictingUsers = lockResult.getConflicts().get(uri);
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Cannot lock ");
                messageBuilder.append(uri);
                messageBuilder.append(" because it is already locked by ");
                messageBuilder.append(String.join("; ", conflictingUsers));
                String message = messageBuilder.toString();
                logger.info(message);
                throw new WriteException(message);
            }
        } catch (IOException e) {
            throw new WriteException(e.getMessage(), e);
        }
    }
}
