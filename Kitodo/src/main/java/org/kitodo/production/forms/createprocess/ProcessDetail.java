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
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataformat.Division;
import org.kitodo.api.dataformat.IncludedStructuralElement;
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
    public void copy() throws IOException {
        container.copy(this);
        refreshPage();
    }

    /**
     * This method is triggered when the user clicks the delete metadata button.
     */
    public void delete() throws IOException {
        container.remove(this);
        refreshPage();
    }

    private void refreshPage() throws IOException {
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        if (Objects.nonNull(currentInstance)) {
            ExternalContext externalContext = currentInstance.getExternalContext();
            externalContext.redirect(((HttpServletRequest) externalContext.getRequest()).getRequestURI());
        }
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
     * collection obtainable from {@link IncludedStructuralElement#getMetadata()}.
     *
     * @return the metadata from this row
     * @throws InvalidMetadataValueException
     *             if the metadata form contains syntactically wrong input
     */
    public abstract Collection<Metadata> getMetadata() throws InvalidMetadataValueException;

    /**
     * If the metadata entry addresses a property of the structure, returns a
     * pair of the setter and the value to set; else {@code null}. This method
     * it to be called when saving the data.
     *
     * @return if data is to be written a pair of the setter of the
     *         {@link IncludedStructuralElement} and the value to set, else null
     * @throws InvalidMetadataValueException
     *             if the metadata form contains syntactically wrong input
     * @throws NoSuchMetadataFieldException
     *             if the field configured in the rule set does not exist
     */
    abstract Pair<BiConsumer<Division<?>, String>, String> getStructureFieldValue()
            throws InvalidMetadataValueException, NoSuchMetadataFieldException;

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
     * Sets whether this metadata entry is leading for options of other metadata
     * entries. Set to true to tell the application to refresh the metadata
     * panel after this entry was changed, to reflect the option changes in
     * other metadata entries.
     */
    public void setLeading() {
        this.leading = true;
    }
}
