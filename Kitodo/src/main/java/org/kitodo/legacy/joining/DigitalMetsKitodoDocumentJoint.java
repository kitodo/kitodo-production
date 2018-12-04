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

import de.sub.goobi.metadaten.Metadaten;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale.LanguageRange;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
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
import org.kitodo.data.database.beans.User;
import org.kitodo.helper.Helper;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.dataeditor.RulesetManagementService;
import org.kitodo.services.dataformat.MetsService;
import org.kitodo.services.file.FileService;

public class DigitalMetsKitodoDocumentJoint implements DigitalDocumentInterface, MetsModsInterface {
    private static final Logger logger = LogManager.getLogger(DigitalMetsKitodoDocumentJoint.class);

    private final ServiceManager serviceLoader = new ServiceManager();
    private final MetsService metsService = serviceLoader.getMetsService();
    private final FileService fileService = serviceLoader.getFileService();
    RulesetManagementService rulesetManagementService = serviceLoader.getRulesetManagementService();

    private MetsXmlElementAccessInterface workpiece = metsService.createMets();
    private RulesetManagementInterface ruleset;

    private List<LanguageRange> priorityList;

    // hat Regelsatz und leeres Werkstück
    public DigitalMetsKitodoDocumentJoint(RulesetManagementInterface ruleset) {
        this();
        this.ruleset = ruleset;
    }

    // hat leeres Werkstück und keinen Regelsatz
    public DigitalMetsKitodoDocumentJoint() {
        this.ruleset = rulesetManagementService.getRulesetManagement();
        this.workpiece = metsService.createMets();

        User user = new Metadaten().getCurrentUser();
        String metadataLanguage = user != null ? user.getMetadataLanguage()
                : Helper.getRequestParameter("Accept-Language");
        this.priorityList = LanguageRange.parse(metadataLanguage != null ? metadataLanguage : "en");
    }

    @Override
    public void addAllContentFiles() {
        // In the legacy implementation, this method must be called to fully
        // build the object-internal data structure after reading a file. Since
        // in the new implementation each method does everything it should from
        // the start, and not just half of it, this function is empty.
    }

    @Override
    public DocStructInterface createDocStruct(DocStructTypeInterface docStructType) {
        logger.log(Level.TRACE, "createDocStruct(docStructType: {})", docStructType);
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
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

    @Override
    public DigitalDocumentInterface getDigitalDocument() throws PreferencesException {
        return this;
    }

    @Override
    public FileSetInterface getFileSet() {
        return new FileSetJoint(workpiece.getFileGrp());
    }

    @Override
    public DocStructInterface getLogicalDocStruct() {
        return new LogicalDocStructJoint(workpiece.getStructMap(), ruleset, priorityList);
    }

    @Override
    public DocStructInterface getPhysicalDocStruct() {
        return new FileSetJoint(workpiece.getFileGrp());
    }

    @Override
    public void overrideContentFiles(List<String> images) {
        logger.log(Level.TRACE, "overrideContentFiles(images: {})", images);
        // TODO Auto-generated method stub
    }

    @Override
    public void read(String path) throws ReadException {
        URI uri = new File(path).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (InputStream in = fileService.read(uri, lockResult)) {
                    logger.info("Reading METS/Kitodo document {}", uri.toString());
                    workpiece.read(in);
                }
            } else {
                throw new ReadException(createLockErrorMessage(uri, lockResult));
            }
        } catch (IOException e) {
            throw new ReadException(e.getMessage(), e);
        }
    }

    @Override
    public void setDigitalDocument(DigitalDocumentInterface digitalDocument) {
        DigitalMetsKitodoDocumentJoint metsKitodoDocument = (DigitalMetsKitodoDocumentJoint) digitalDocument;
        this.workpiece = metsKitodoDocument.workpiece;
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
                throw new WriteException(createLockErrorMessage(uri, lockResult));
            }
        } catch (IOException e) {
            throw new WriteException(e.getMessage(), e);
        }
    }
}
