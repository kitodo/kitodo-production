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

package org.kitodo.metadata.elements.renderable;

import java.util.ArrayList;
import java.util.List;

import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.exceptions.UnreachableCodeException;
import org.kitodo.metadata.MetadataProcessor;

/**
 * Backing bean for a single line input box element to edit a metadata
 * renderable by JSF.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Edit extends RenderableMetadata
        implements RenderableGroupableMetadata, SingleValueRenderableMetadata {

    /**
     * Holds the content of the input box.
     */
    private String value;

    /**
     * Constructor. Creates a RenderableEdit.
     *
     * @param metadataType
     *            metadata type editable by this drop-down list
     * @param binding
     *            metadata group that shall instantly be updated if a setter is
     *            invoked
     * @param container
     *            metadata group this drop-down list is showing in
     */
    public Edit(MetadataTypeInterface metadataType, MetadataGroupInterface binding,
                RenderableMetadataGroup container) {

        super(metadataType, binding, container);
        if (binding != null) {
            for (MetadataInterface data : binding.getMetadataByType(metadataType.getName())) {
                addContent(data);
            }
        }
    }

    /**
     * Adds the data passed from the metadata element as content to the input.
     * If there is data already (shouldn’t be, but however) it is appended for
     * not being lost.
     *
     * @param data
     *            data to add
     */
    @Override
    public void addContent(MetadataInterface data) {
        if (value == null || value.length() == 0) {
            value = data.getValue();
        } else {
            value += "; " + data.getValue();
        }
    }

    /**
     * Returns the edit field value.
     *
     * @return the value from or for the edit field
     *
     * @see SingleValueRenderableMetadata#getValue()
     */
    @Override
    public String getValue() {
        return value != null ? value : "";
    }

    /**
     * Sets the value or saves the value entered by the user.
     *
     * @param value
     *            value to set
     *
     * @see SingleValueRenderableMetadata#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
        this.value = value;
        updateBinding();
    }

    /**
     * Returns the value of this edit component as metadata element.
     *
     * @return a list with one metadata element with the value of this component
     * @see RenderableGroupableMetadata#toMetadata()
     */
    @Override
    public List<MetadataInterface> toMetadata() {
        List<MetadataInterface> result = new ArrayList<>(1);
        result.add(getMetadata(value));
        return result;
    }

    /**
     * Specialised version of updateBinding() which is capable to update a
     * metadata type of kind “person” if the input box is part of a
     * RenderablePersonMetadataGroup.
     *
     * @see RenderableMetadata#updateBinding()
     */
    @Override
    protected void updateBinding() {
        if (binding != null) {
            String typeName = metadataType.getName();
            String personType = PersonMetadataGroup.getPersonType(typeName);
            if (personType == null) {
                super.updateBinding();
            } else {
                for (PersonInterface found : binding.getPersonByType(personType)) {
                    switch (PersonMetadataGroup.getPersonField(typeName)) {
                        case FIRSTNAME:
                            found.setFirstName(value);
                            break;
                        case LASTNAME:
                            found.setLastName(value);
                            break;
                        case NORMDATA_RECORD:
                            if (value != null && value.length() > 0
                                    && !value.equals(ConfigCore.getParameter(ParameterCore.AUTHORITY_DEFAULT, ""))) {
                                String[] authorityFile = MetadataProcessor.parseAuthorityFileArgs(value);
                                found.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
                            }
                            break;
                        default:
                            throw new UnreachableCodeException("Complete switch");
                    }
                }
            }
        }
    }

}
