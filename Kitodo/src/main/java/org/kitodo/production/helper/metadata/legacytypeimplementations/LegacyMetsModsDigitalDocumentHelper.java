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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

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
import org.kitodo.api.ugh.MetsModsImportExportInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.User;
import org.kitodo.helper.metadata.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataProcessor;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataeditor.RulesetManagementService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;

/**
 * Connects a legacy METS MODS and digital document to a workpiece. This is a
 * soldering class to keep legacy code operational which is about to be removed.
 * Do not use this class.
 */
public class LegacyMetsModsDigitalDocumentHelper implements MetsModsImportExportInterface {
    private static final Logger logger = LogManager.getLogger(LegacyMetsModsDigitalDocumentHelper.class);

    private static final MetsService metsService = ServiceManager.getMetsService();
    private static final FileService fileService = ServiceManager.getFileService();
    private static final RulesetManagementService rulesetManagementService = ServiceManager
            .getRulesetManagementService();

    /**
     * The workpiece accessed via this soldering class.
     */
    private MetsXmlElementAccessInterface workpiece = metsService.createMetsXmlElementAccess();

    /**
     * The current ruleset.
     */
    private RulesetManagementInterface ruleset;

    /**
     * The user’s meta-data language priority list.
     */
    private List<LanguageRange> priorityList;

    /**
     * Creates a new legacy METS MODS digital document helper.
     */
    public LegacyMetsModsDigitalDocumentHelper() {
        this.ruleset = rulesetManagementService.getRulesetManagement();
        this.workpiece = metsService.createMetsXmlElementAccess();

        try {
            User user = new MetadataProcessor().getCurrentUser();
            String metadataLanguage = user != null ? user.getMetadataLanguage()
                    : Helper.getRequestParameter("Accept-Language");
            this.priorityList = LanguageRange.parse(metadataLanguage != null ? metadataLanguage : "en");
        } catch (NullPointerException e) {
            /*
             * new Metadaten() throws a NullPointerException in asynchronous
             * export because there is no Faces context then.
             */
            logger.catching(Level.TRACE, e);
            this.priorityList = LanguageRange.parse("en");
        }
    }

    /**
     * Creates a new legacy METS MODS digital document helper with a ruleset.
     * 
     * @param ruleset
     *            ruleset to set
     */
    public LegacyMetsModsDigitalDocumentHelper(RulesetManagementInterface ruleset) {
        this();
        this.ruleset = ruleset;
    }

    /**
     * Creates a new legacy METS MODS digital document helper with a workpiece.
     * 
     * @param ruleset
     *            ruleset to set
     * @param workpiece
     *            workpiece to set
     */
    public LegacyMetsModsDigitalDocumentHelper(RulesetManagementInterface ruleset,
            MetsXmlElementAccessInterface workpiece) {

        this(ruleset);
        this.workpiece = workpiece;
    }

    public void addAllContentFiles() {
        /*
         * In the legacy implementation, this method must be called to fully
         * build the object-internal data structure after reading a file. Since
         * in the new implementation each method does everything it should from
         * the start, and not just half of it, this function is empty.
         */
    }

    public LegacyDocStructHelperInterface createDocStruct(LegacyLogicalDocStructTypeHelper docStructType) {
        if (!docStructType.equals(LegacyInnerPhysicalDocStructTypePageHelper.INSTANCE)) {
            return new LegacyLogicalDocStructHelper(metsService.createDivXmlElementAccess(), null, ruleset, priorityList);
        } else {
            return new LegacyInnerPhysicalDocStructHelper();
        }
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

    public LegacyMetsModsDigitalDocumentHelper getDigitalDocument() throws PreferencesException {
        return this;
    }

    public LegacyFileSetDocStructHelper getFileSet() {
        return new LegacyFileSetDocStructHelper(workpiece.getFileGrp());
    }

    public LegacyDocStructHelperInterface getLogicalDocStruct() {
        return new LegacyLogicalDocStructHelper(workpiece.getStructMap(), null, ruleset, priorityList);
    }

    public LegacyDocStructHelperInterface getPhysicalDocStruct() {
        return new LegacyFileSetDocStructHelper(workpiece.getFileGrp());
    }

    /**
     * Returns the workpiece of the legacy METS/MODS digital document helper.
     * 
     * @return the workpiece
     */
    public MetsXmlElementAccessInterface getWorkpiece() {
        return workpiece;
    }

    public void overrideContentFiles(List<String> images) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void read(String path) throws ReadException {
        URI uri = new File(path).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (InputStream in = fileService.read(uri, lockResult)) {
                    logger.info("Reading {}", uri.toString());
                    workpiece.read(in);
                }
            } else {
                throw new ReadException(createLockErrorMessage(uri, lockResult));
            }
        } catch (IOException e) {
            throw new ReadException(e.getMessage(), e);
        }
    }

    public void setDigitalDocument(LegacyMetsModsDigitalDocumentHelper metsKitodoDocument) {
        this.workpiece = metsKitodoDocument.workpiece;
    }

    public void setLogicalDocStruct(LegacyDocStructHelperInterface docStruct) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void setPhysicalDocStruct(LegacyDocStructHelperInterface docStruct) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void write(String filename) throws PreferencesException, WriteException {
        URI uri = new File(filename).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (OutputStream out = fileService.write(uri, lockResult)) {
                    logger.info("Saving {}", uri.toString());
                    workpiece.save(out);
                }
            } else {
                throw new WriteException(createLockErrorMessage(uri, lockResult));
            }
        } catch (IOException e) {
            throw new WriteException(e.getMessage(), e);
        }
    }

    /**
     * This method generates a comprehensible log message in case something was
     * overlooked and one of the unimplemented methods should ever be called in
     * operation. The name was chosen deliberately short in order to keep the
     * calling code clear. This method must be implemented in every class
     * because it uses the logger tailored to the class.
     * 
     * @param exception
     *            created {@code UnsupportedOperationException}
     * @return the exception
     */
    private static RuntimeException andLog(UnsupportedOperationException exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StringBuilder buffer = new StringBuilder(255);
        buffer.append(stackTrace[1].getClassName());
        buffer.append('.');
        buffer.append(stackTrace[1].getMethodName());
        buffer.append("()");
        if (stackTrace[1].getLineNumber() > -1) {
            buffer.append(" line ");
            buffer.append(stackTrace[1].getLineNumber());
        }
        buffer.append(" unexpectedly called unimplemented ");
        buffer.append(stackTrace[0].getMethodName());
        buffer.append("()");
        if (exception.getMessage() != null) {
            buffer.append(": ");
            buffer.append(exception.getMessage());
        }
        logger.error(buffer.toString());
        return exception;
    }

    public void setContentIDs(String contentIDs) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setDigiprovPresentation(String digiprovPresentation) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setDigiprovPresentationAnchor(String digiprovPresentationAnchor) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setDigiprovReference(String digiprovReference) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setDigiprovReferenceAnchor(String digiprovReferenceAnchor) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setMptrAnchorUrl(String mptrAnchorUrl) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setMptrUrl(String mptrUrl) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setPurlUrl(String purlUrl) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setRightsOwner(String rightsOwner) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setRightsOwnerContact(String rightsOwnerContact) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setRightsOwnerLogo(String rightsOwnerLogo) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setRightsOwnerSiteURL(String rightsOwnerSiteURL) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    public void setWriteLocal(boolean writeLocalFileGroup) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }
}
