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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;


public abstract class AdditionalDetailsTableRow implements Serializable {

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
     * The label of this row.
     */
    protected final String label;

    /**
     * Metadata panel on which this row is showing.
     */
    protected final AdditionalDetailsTab tab;

    /**
     * Parental metadata group.
     */
    private FieldedAdditionalDetailsTableRow container;

    /**
     * Creates a new metadata panel row.
     *
     * @param tab
     *            additional details tab on which this row is showing
     * @param container
     *            parental metadata group
     * @param label
     *            the label of this row
     */
    AdditionalDetailsTableRow(AdditionalDetailsTab tab, FieldedAdditionalDetailsTableRow container, String label) {
        this.tab = tab;
        this.container = container;
        this.label = label;
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
    abstract Pair<Method, Object> getStructureFieldValue()
            throws InvalidMetadataValueException, NoSuchMetadataFieldException;

    /**
     * Returns if the field is not defined by the rule set. The front-end should
     * show some kind of warning sign then.
     *
     * @return if the field is not defined by the rule set
     */
    public abstract boolean isUndefined();
}
