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

package org.kitodo.production.forms.createprocess;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;

import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;

public abstract class ProcessDetail implements Serializable {
    /**
     * Describes the relationship between the domain in the rule set and the
     * mdSec in the METS.
     */
    protected static final EnumMap<Domain, MdSec> DOMAIN_TO_MDSEC = new EnumMap<>(Domain.class);

    static {
        DOMAIN_TO_MDSEC.put(Domain.DESCRIPTION, MdSec.DMD_SEC);
        DOMAIN_TO_MDSEC.put(Domain.DIGITAL_PROVENANCE, MdSec.DIGIPROV_MD);
        DOMAIN_TO_MDSEC.put(Domain.RIGHTS, MdSec.RIGHTS_MD);
        DOMAIN_TO_MDSEC.put(Domain.SOURCE, MdSec.SOURCE_MD);
        DOMAIN_TO_MDSEC.put(Domain.TECHNICAL, MdSec.TECH_MD);
    }

    /**
     * Parental metadata group.
     */
    protected ProcessFieldedMetadata container;

    /**
     * The label of this row.
     */
    protected final String label;

    /**
     * Whether this metadata entry is leading for options of other metadata
     * entries.
     */
    protected boolean leading = false;

    /**
     * Creates a new metadata panel row.
     *
     * @param label
     *            the label of this row
     */
    ProcessDetail(ProcessFieldedMetadata container, String label) {
        this.container = container;
        this.label = label;
    }

    /**
     * This method is triggered when the user clicks the copy metadata button.
     */
    public void copy() throws IOException, InvalidMetadataValueException, NoSuchMetadataFieldException {
        container.copy(this);
        container.preserve();
    }

    /**
     * This method is triggered when the user clicks the delete metadata button.
     */
    public void delete() throws IOException, InvalidMetadataValueException, NoSuchMetadataFieldException {
        container.remove(this);
    }

    public abstract String getMetadataID();

    /**
     * Returns the type of input to be rendered in this row. One of the
     * following values:
     *

     *
     * @return the type of input to be rendered
     */
    public abstract String getInput();

    /**
     * Returns the label of this row.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the metadata from this row, as far as it has to be stored in the
     * collection obtainable from {@link LogicalDivision#getMetadata()}.
     *
     * @return the metadata from this row
     * @throws InvalidMetadataValueException
     *             if the metadata form contains syntactically wrong input
     */
    public abstract Collection<Metadata> getMetadataWithFilledValues() throws InvalidMetadataValueException;

    /**
     * Returns the metadata from this row.
     * @param skipEmpty boolean to filter metadata with empty value or not.
     * @return the metadata from this row
     * @throws InvalidMetadataValueException
     *             if the metadata form contains syntactically wrong input
     */
    public abstract Collection<Metadata> getMetadata(boolean skipEmpty) throws InvalidMetadataValueException;

    /**
     * Returns whether this metadata entry is leading for options of other
     * metadata entries. If true, the application must refresh the metadata
     * panel after this entry was changed to reflect the option changes in other
     * metadata entries.
     *
     * @return whether this metadata entry is leading
     */
    public boolean isLeading() {
        return leading;
    }

    /**
     * Returns if the field is not defined by the rule set. The front-end should
     * show some kind of warning sign then.
     *
     * @return if the field is not defined by the rule set
     */
    public abstract boolean isUndefined();

    /**
     * Returns if the field is required in the rule set.
     *
     * @return if the field is required in the rule set
     */
    public abstract boolean isRequired();

    /**
     * Returns if the field is valid.
     * @return if the field is valid
     */
    public abstract boolean isValid();

    /**
     * Get occurrences of this metadata in container.
     * @return occurrences
     */
    public int getOccurrences() {
        return container.getOccurrences(this.getMetadataID());
    }

    public void preserve() throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        container.preserve();
    }

    public abstract int getMinOccurs();

    /**
     * Sets whether this metadata entry is leading for options of other metadata
     * entries. Set to true to tell the application to refresh the metadata
     * panel after this entry was changed, to reflect the option changes in
     * other metadata entries.
     */
    public void setLeading() {
        this.leading = true;
    }
}
