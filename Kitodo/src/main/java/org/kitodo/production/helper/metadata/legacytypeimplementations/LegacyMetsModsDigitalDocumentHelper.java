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
import org.kitodo.data.database.beans.User;
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
public class LegacyMetsModsDigitalDocumentHelper {
    private static final Logger logger = LogManager.getLogger(LegacyMetsModsDigitalDocumentHelper.class);

    /**
     * For each meta data element of this type that is associated with a
     * DocStruct element of the logical structure tree of a digital document, a
     * METS pointer element will be created during export.
     */
    @Deprecated
    public static final String CREATE_MPTR_ELEMENT_TYPE = "MetsPointerURL";

    /**
     * If there is a meta data element of this type associated with a DocStruct
     * element of the logical structure tree of a digital document, a LABEL
     * attribute will be attached to the logical div element during export which
     * will have assigned the value assigned to the last meta data element of
     * this type associated with the DocStruct element.
     */
    @Deprecated
    public static final String CREATE_LABEL_ATTRIBUTE_TYPE = "TitleDocMain";

    /**
     * If there is a meta data element of this type associated with a DocStruct
     * element of the logical structure tree of a digital document, an
     * ORDERLABEL attribute will be attached to the logical div element during
     * export which will have assigned the value assigned to the last meta data
     * element of this type associated with the DocStruct element.
     */
    @Deprecated
    public static final String CREATE_ORDERLABEL_ATTRIBUTE_TYPE = "TitleDocMainShort";

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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public LegacyMetsModsDigitalDocumentHelper(RulesetManagementInterface ruleset,
            MetsXmlElementAccessInterface workpiece) {

        this(ruleset);
        this.workpiece = workpiece;
    }

    /**
     * Add all content files to the digital document according to the
     * pathimagefiles meta-data. The pages in the physical DocStruct must
     * already exist!
     */
    @Deprecated
    public void addAllContentFiles() {
        /*
         * In the legacy implementation, this method must be called to fully
         * build the object-internal data structure after reading a file. Since
         * in the new implementation each method does everything it should from
         * the start, and not just half of it, this function is empty.
         */
    }

    /**
     * Creates a document structure with the given document structure type for
     * the Digital Document.
     *
     * @param docStructType
     *            document structure type for the new document structure
     * @return the new document structure
     */
    @Deprecated
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

    @Deprecated
    public LegacyMetsModsDigitalDocumentHelper getDigitalDocument() {
        return this;
    }

    @Deprecated
    public LegacyFileSetDocStructHelper getFileSet() {
        return new LegacyFileSetDocStructHelper(workpiece.getFileGrp());
    }

    @Deprecated
    public LegacyDocStructHelperInterface getLogicalDocStruct() {
        return new LegacyLogicalDocStructHelper(workpiece.getStructMap(), null, ruleset, priorityList);
    }

    @Deprecated
    public LegacyDocStructHelperInterface getPhysicalDocStruct() {
        return new LegacyFileSetDocStructHelper(workpiece.getFileGrp());
    }

    /**
     * Returns the workpiece of the legacy METS/MODS digital document helper.
     * 
     * @return the workpiece
     */
    @Deprecated
    public MetsXmlElementAccessInterface getWorkpiece() {
        return workpiece;
    }

    @Deprecated
    public void overrideContentFiles(List<String> images) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Reads a file and creates a digital document instance.
     *
     * @param path
     *            full path to file which should be read
     * @throws IOException
     *             may be thrown if reading fails
     */
    @Deprecated
    public void read(String path) throws IOException {
        URI uri = new File(path).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (InputStream in = fileService.read(uri, lockResult)) {
                    logger.info("Reading {}", uri.toString());
                    workpiece.read(in);
                }
            } else {
                throw new IOException(createLockErrorMessage(uri, lockResult));
            }
        }
    }

    @Deprecated
    public void setDigitalDocument(LegacyMetsModsDigitalDocumentHelper metsKitodoDocument) {
        this.workpiece = metsKitodoDocument.workpiece;
    }

    @Deprecated
    public void setLogicalDocStruct(LegacyDocStructHelperInterface docStruct) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public void setPhysicalDocStruct(LegacyDocStructHelperInterface docStruct) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * Writes the content of the DigitalDocument instance to a file. The file
     * format must already have a DigitalDocument instance.
     *
     * @param filename
     *            full path to the file
     * @throws IOException
     *             may be thrown if writing fails
     */
    @Deprecated
    public void write(String filename) throws IOException {
        URI uri = new File(filename).toURI();

        try (LockResult lockResult = fileService.tryLock(uri, LockingMode.EXCLUSIVE)) {
            if (lockResult.isSuccessful()) {
                try (OutputStream out = fileService.write(uri, lockResult)) {
                    logger.info("Saving {}", uri.toString());
                    workpiece.save(out);
                }
            } else {
                throw new IOException(createLockErrorMessage(uri, lockResult));
            }
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

    /**
     * Sets the content IDs of the METS/MODS import export.
     *
     * @param contentIDs
     *            the content IDs to set
     */
    @Deprecated
    public void setContentIDs(String contentIDs) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the digital provenience presentation of the METS/MODS import export.
     *
     * @param digiprovPresentation
     *            the digital provenience presentation to set
     */
    @Deprecated
    public void setDigiprovPresentation(String digiprovPresentation) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the digital provenience presentation anchor of the METS/MODS import
     * export.
     *
     * @param digiprovPresentationAnchor
     *            the digital provenience presentation anchor to set
     */
    @Deprecated
    public void setDigiprovPresentationAnchor(String digiprovPresentationAnchor) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the digital provenience reference of the METS/MODS import export.
     *
     * @param digiprovReference
     *            the digital provenience reference to set
     */
    @Deprecated
    public void setDigiprovReference(String digiprovReference) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the digital provenience reference anchor of the METS/MODS import
     * export.
     *
     * @param digiprovReferenceAnchor
     *            the digital provenience reference anchor to set
     */
    @Deprecated
    public void setDigiprovReferenceAnchor(String digiprovReferenceAnchor) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the mets:mptr anchor URL of the METS/MODS import export.
     *
     * @param mptrAnchorUrl
     *            the mets:mptr anchor URL to set
     */
    @Deprecated
    public void setMptrAnchorUrl(String mptrAnchorUrl) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Adds a mets:mptr URL to the METS/MODS import export.
     *
     * @param mptrUrl
     *            the mets:mptr URL to set
     */
    @Deprecated
    public void setMptrUrl(String mptrUrl) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the purl URL of the METS/MODS import export.
     *
     * @param purlUrl
     *            the purl URL to set
     */
    @Deprecated
    public void setPurlUrl(String purlUrl) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the rights owner of the METS/MODS import export.
     *
     * @param rightsOwner
     *            the rights owner to set
     */
    @Deprecated
    public void setRightsOwner(String rightsOwner) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the rights owner contact of the METS/MODS import export.
     *
     * @param rightsOwnerContact
     *            the rights owner contact to set
     */
    @Deprecated
    public void setRightsOwnerContact(String rightsOwnerContact) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the rights owner logo of the METS/MODS import export.
     *
     * @param rightsOwnerLogo
     *            the rights owner logo to set
     */
    @Deprecated
    public void setRightsOwnerLogo(String rightsOwnerLogo) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets the rights owner site URL of the METS/MODS import export.
     *
     * @param rightsOwnerSiteURL
     *            the rights owner site URL to set
     */
    @Deprecated
    public void setRightsOwnerSiteURL(String rightsOwnerSiteURL) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }

    /**
     * Sets whether the METS/MODS import export shall write a local file group.
     *
     * @param writeLocalFileGroup
     *            whether the METS/MODS import export shall write a local file
     *            group
     */
    @Deprecated
    public void setWriteLocal(boolean writeLocalFileGroup) {
        /*
         * This is already done by the schema service. There is nothing to do
         * here.
         */
    }
}
