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

    @Override
    public DigitalDocumentInterface getDigitalDocument() throws PreferencesException {
        logger.log(Level.TRACE, "getDigitalDocument()");
        return new DigitalMetsKitodoDocumentJoint(workpiece);
    }

    @Override
    public void read(String path) throws ReadException {
        logger.log(Level.TRACE, "read(path: \"{}\")", path);
        URI uri = new File(path).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (InputStream in = fileService.read(uri, lockResult)) {
                    workpiece.read(in);
                }
            } else {
                Collection<String> conflictingUsers = lockResult.getConflicts().get(uri);
                StringBuilder message = new StringBuilder();
                message.append("Cannot lock ");
                message.append(uri);
                message.append(" because it is already locked by ");
                message.append(String.join("; ", conflictingUsers));
                throw new ReadException(message.toString());
            }
        } catch (IOException e) {
            throw new ReadException(e.getMessage(), e);
        }
    }

    @Override
    public void setDigitalDocument(DigitalDocumentInterface digitalDocument) {
        logger.log(Level.TRACE, "setDigitalDocument(digitalDocument: {})", digitalDocument);
        this.workpiece = ((DigitalMetsKitodoDocumentJoint) digitalDocument).workpiece;
    }

    @Override
    public void write(String filename) throws PreferencesException, WriteException {
        logger.log(Level.TRACE, "write(filename: {})");
        // TODO Auto-generated method stub
    }

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
    public void setLogicalDocStruct(DocStructInterface docStruct) {
        logger.log(Level.TRACE, "setLogicalDocStruct(docStruct: {})", docStruct);
        // TODO Auto-generated method stub
    }

    @Override
    public void setPhysicalDocStruct(DocStructInterface docStruct) {
        logger.log(Level.TRACE, "setPhysicalDocStruct(docStruct: {})", docStruct);
        // TODO Auto-generated method stub
    }
}
