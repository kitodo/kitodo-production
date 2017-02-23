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

import java.sql.SQLException;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.ProcessObject;

/**
 * A CopierData object contains all the data the data copier has access to. It
 * has been implemented as an own bean class to allow to easily add variables
 * later without needing to extend many interfaces.
 *
 * @author Matthias Ronge
 */
public class CopierData {

    /**
     * A metadata selector relative to which the data shall be read during
     * copying.
     */
    private final MetadataSelector destination;

    /**
     * The workspace file to modify
     */
    private final Fileformat fileformat;

    /**
     * The Goobi process corresponding to the workspace file
     */
    private final Object process;

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
    public CopierData(Fileformat fileformat, Object process) {
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
    DigitalDocument getDigitalDocument() {
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
    public DocStruct getLogicalDocStruct() {
        return getDigitalDocument().getLogicalDocStruct();
    }

    /**
     * Returns the ruleset to be used with the fileformat.
     *
     * @return the required ruleset.
     */
    public Prefs getPreferences() throws SQLException {
        if (process instanceof ProcessObject) {
            return MySQLHelper.getRulesetForId(((ProcessObject) process).getRulesetId()).getPreferences();
        } else {
            return ((Prozess) process).getRegelsatz().getPreferences();
        }
    }

    /**
     * Returns the process title.
     *
     * @return the process title
     */
    public String getProcessTitle() {
        if (process instanceof Prozess) {
            return ((Prozess) process).getTitel();
        } else if (process instanceof ProcessObject) {
            return ((ProcessObject) process).getTitle();
        } else {
            return String.valueOf(process);
        }
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
