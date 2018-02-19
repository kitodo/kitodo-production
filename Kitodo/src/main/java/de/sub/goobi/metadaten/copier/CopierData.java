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

package de.sub.goobi.metadaten.copier;

import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

/**
 * A CopierData object contains all the data the data copier has access to. It
 * has been implemented as an own bean class to allow to easily add variables
 * later without needing to extend many interfaces.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CopierData {
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * A metadata selector relative to which the data shall be read during
     * copying.
     */
    private final MetadataSelector destination;

    /**
     * The workspace file to modify.
     */
    private final FileformatInterface fileformat;

    /**
     * The Goobi process corresponding to the workspace file.
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
     *            the related goobi process
     */
    public CopierData(FileformatInterface fileformat, Process process) {
        this.fileformat = fileformat;
        this.process = process;
        this.destination = null;
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
    DigitalDocumentInterface getDigitalDocument() {
        try {
            return fileformat.getDigitalDocument();
        } catch (PreferencesException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Returns the top-level element of the logical document structure tree.
     *
     * @return the logical document structure
     */
    public DocStructInterface getLogicalDocStruct() {
        return getDigitalDocument().getLogicalDocStruct();
    }

    /**
     * Returns the ruleset to be used with the fileformat.
     *
     * @return the required ruleset.
     */
    public PrefsInterface getPreferences() {
        return serviceManager.getRulesetService().getPreferences((process).getRuleset());
    }

    /**
     * Returns the process title.
     *
     * @return the process title
     */
    public String getProcessTitle() {
        return (process).getTitle();

    }

    /**
     * Returns a string that textually represents this bean.
     *
     * @return a string representation of this object
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{fileformat: " + fileformat.toString() + ", process: " + getProcessTitle() + '}';
    }
}
