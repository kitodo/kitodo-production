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
import java.net.URI;
import java.util.List;
import java.util.Locale.LanguageRange;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataeditor.RulesetManagementService;

/**
 * Connects a legacy METS MODS and digital document to a workpiece. This is a
 * soldering class to keep legacy code operational which is about to be removed.
 * Do not use this class.
 */
public class LegacyMetsModsDigitalDocumentHelper {
    private static final Logger logger = LogManager.getLogger(LegacyMetsModsDigitalDocumentHelper.class);

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

    private static final RulesetManagementService rulesetManagementService = ServiceManager
            .getRulesetManagementService();

    /**
     * The workpiece accessed via this soldering class.
     */
    private Workpiece workpiece;

    /**
     * The current ruleset.
     */
    private RulesetManagementInterface ruleset;

    /**
     * The user’s metadata language priority list.
     */
    private List<LanguageRange> priorityList;

    /**
     * Creates a new legacy METS MODS digital document helper.
     */
    @Deprecated
    public LegacyMetsModsDigitalDocumentHelper() {
        this.ruleset = rulesetManagementService.getRulesetManagement();
        this.workpiece = new Workpiece();

        try {
            User user = ServiceManager.getUserService().getAuthenticatedUser();
            String metadataLanguage = user != null ? user.getMetadataLanguage()
                    : Helper.getRequestParameter("Accept-Language");
            this.priorityList = LanguageRange.parse(! metadataLanguage.isEmpty() ? metadataLanguage : "en");
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
    public LegacyMetsModsDigitalDocumentHelper(RulesetManagementInterface ruleset, Workpiece workpiece) {
        this(ruleset);
        this.workpiece = workpiece;
    }

    @Deprecated
    public LegacyMetsModsDigitalDocumentHelper getDigitalDocument() {
        return this;
    }

    @Deprecated
    public LegacyFileSetDocStructHelper getFileSet() {
        return new LegacyFileSetDocStructHelper(workpiece.getMediaUnit().getChildren());
    }

    @Deprecated
    public LegacyDocStructHelperInterface getLogicalDocStruct() {
        return new LegacyLogicalDocStructHelper(workpiece.getLogicalStructure(), null, ruleset, priorityList);
    }

    @Deprecated
    public LegacyDocStructHelperInterface getPhysicalDocStruct() {
        return new LegacyFileSetDocStructHelper(workpiece.getMediaUnit().getChildren());
    }

    /**
     * Returns the workpiece of the legacy METS/MODS digital document helper.
     *
     * @return the workpiece
     */
    public Workpiece getWorkpiece() {
        return workpiece;
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
        workpiece = ServiceManager.getMetsService().loadWorkpiece(uri);
    }

    @Deprecated
    public void setDigitalDocument(LegacyMetsModsDigitalDocumentHelper metsKitodoDocument) {
        this.workpiece = metsKitodoDocument.workpiece;
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
        ServiceManager.getMetsService().saveWorkpiece(workpiece, uri);
    }
}
