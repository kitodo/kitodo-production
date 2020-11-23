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

package org.kitodo.production.metadata.copier;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;

/**
 * A CopierData object contains all the data the data copier has access to. It
 * has been implemented as an own bean class to allow to easily add variables
 * later without needing to extend many interfaces.
 */
public class CopierData {

    /**
     * A metadata selector relative to which the data shall be read during
     * copying.
     */
    private final MetadataSelector destination;

    /**
     * The workspace file to modify.
     */
    private final LegacyMetsModsDigitalDocumentHelper fileformat;

    /**
     * The Production process corresponding to the workspace file.
     */
    private final Process process;

    /**
     * Creates a new CopierData bean with an additional destination metadata
     * selector.
     *
     * @param data
     *            data bean without or with destination metadata selector
     * @param destination
     *            destination metadata selector to use
     */
    public CopierData(CopierData data, MetadataSelector destination) {
        this.fileformat = data.fileformat;
        this.process = data.process;
        this.destination = destination;
    }

    /**
     * Creates a new CopierData bean.
     *
     * @param fileformat
     *            the document to modify
     * @param process
     *            the related Production process
     */
    public CopierData(LegacyMetsModsDigitalDocumentHelper fileformat, Process process) {
        this.fileformat = fileformat;
        this.process = process;
        this.destination = null;
    }

    /**
     * Creates a new CopierData bean.
     *
     * @param fileformat
     *            the document to modify
     * @param template
     *            the related Production process
     */
    public CopierData(LegacyMetsModsDigitalDocumentHelper fileformat, Template template) {
        this.fileformat = fileformat;
        this.destination = null;
        this.process = new Process();
    }

    /**
     * Returns the destination metadata selector relative to which the data
     * shall be read during copying.
     *
     * @return the destination metadata selector
     */
    public MetadataSelector getDestination() {
        return destination;
    }

    /**
     * Returns the digital document contained in the fileformat passed-in in the
     * constructor.
     *
     * @return the digital document
     */
    LegacyMetsModsDigitalDocumentHelper getDigitalDocument() {
        return fileformat.getDigitalDocument();
    }

    /**
     * Returns the top-level element of the logical document structure tree.
     *
     * @return the logical document structure
     */
    public LegacyDocStructHelperInterface getLogicalDocStruct() {
        return getDigitalDocument().getLogicalDocStruct();
    }

    /**
     * Returns the ruleset to be used with the fileformat.
     *
     * @return the required ruleset.
     */
    public LegacyPrefsHelper getPreferences() {
        return ServiceManager.getRulesetService().getPreferences(process.getRuleset());
    }

    /**
     * Returns the process title.
     *
     * @return the process title
     */
    public Process getProcess() {
        return process;

    }

    /**
     * Returns a string that textually represents this bean.
     *
     * @return a string representation of this object
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{fileformat: " + fileformat.toString() + ", process: " + getProcess().getTitle() + '}';
    }
}
