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

package org.kitodo.production.metadata.elements.renderable;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.helper.metadata.LegacyMetadataHelper;
import org.kitodo.helper.metadata.LegacyMetadataTypeHelper;

/**
 * Specialised RenderableMetadataGroup with fixed fields to edit the internal
 * metadata group type &ldquo;person&rdquo;. A person is a fixed data structure
 * in Goobi with the fields normdata record, first name and last name.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class PersonMetadataGroup extends RenderableMetadataGroup implements RenderableGroupableMetadata {

    /**
     * Holds the fields to show in a RenderablePersonMetadataGroup.
     *
     * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
     */
    enum Field {
        NORMDATA_RECORD("normDataRecord", true),
        FIRSTNAME("vorname", false),
        LASTNAME("nachname", false);

        /**
         * Field constructor. Creates a Field enum constant.
         *
         * @param resourceKey
         *            key string to look up the translated labels for the field
         *            in the messages file
         * @param isIdentifier
         *            boolean property telling whether or not the given field is
         *            an identifier
         */
        Field(String resourceKey, boolean isIdentifier) {
        }
    }

    /**
     * Regular expression to separate the person type from the field identifier.
     */
    private static final Pattern FICTITIOUS_METADATA_TYPE_SCHEME = Pattern.compile("(.+)\\.([^.]+)");

    /**
     * Creates a RenderablePersonMetadataGroup.
     *
     * @param metadataType
     *            metadata type editable by this metadata group
     * @param binding
     *            metadata group this group is showing in
     * @param container
     *            project of the process owning this metadata group
     * @param projectName
     *            whether the user is about to create the metadata group anew or
     *            edit a previously existing one
     * @throws ConfigurationException
     *             if one of the sub-fields was configured to display a
     *             multi-select metadata
     */
    public PersonMetadataGroup(LegacyMetadataTypeHelper metadataType, MetadataGroupInterface binding,
                               RenderableMetadataGroup container, String projectName) throws ConfigurationException {

        super(metadataType, binding, container, getGroupTypeFor(metadataType), projectName);
        checkConfiguration();
        getField(Field.NORMDATA_RECORD).setValue(ConfigCore.getParameter(ParameterCore.AUTHORITY_DEFAULT, ""));
        if (binding != null) {
            for (PersonInterface person : binding.getPersonByType(metadataType.getName())) {
                addContent(person);
            }
        }
    }

    /**
     * Creates a fictitious MetadataGroupType for the given metadata type,
     * assuming it is a person. The method is called from the constructor and
     * thus should not be overloaded.
     *
     * @param type
     *            a metadata type which represents a person
     * @return a fictitious MetadataGroupType with the person’s subfields
     */
    private static MetadataGroupTypeInterface getGroupTypeFor(LegacyMetadataTypeHelper type) {
        throw new UnsupportedOperationException("Dead code pending removal");
    }

    /**
     * Checks whether the configuration is consistent, throws a
     * ConfigurationException otherwise.
     *
     * @throws ConfigurationException
     *             if one of the sub-fields was configured to display a
     *             multi-select metadata
     */
    private void checkConfiguration() throws ConfigurationException {
        for (Entry<String, RenderableGroupableMetadata> entry : members.entrySet()) {
            if (!(entry.getValue() instanceof SingleValueRenderableMetadata)) {
                throw new ConfigurationException(
                        entry.getKey() + " is configured to display a multi-select input element,"
                                + " but the field cannot take multiple values.");
            }
        }
    }

    /**
     * Add the data passed from the metadata element as content to the person
     * record.
     *
     * @param data
     *            data to add
     * @see RenderableGroupableMetadata#addContent(LegacyMetadataHelper)
     */
    @Override
    public void addContent(LegacyMetadataHelper data) {
        if (data instanceof PersonInterface) {
            PersonInterface personData = (PersonInterface) data;
            if (personData.getLastName() != null) {
                getField(Field.LASTNAME).setValue(personData.getLastName());
            }
            if (personData.getFirstName() != null) {
                getField(Field.FIRSTNAME).setValue(personData.getFirstName());
            }
            if (personData.getAuthorityURI() != null) {
                getField(Field.NORMDATA_RECORD).setValue(personData.getAuthorityValue());
            }
        } else {
            String[] lastNameFirstName = data.getValue().split(", ", 2);
            getField(Field.LASTNAME).setValue(lastNameFirstName[0]);
            if (lastNameFirstName.length > 1) {
                getField(Field.FIRSTNAME).setValue(lastNameFirstName[1]);
            }
        }
    }

    /**
     * Returns a specific sub-field of the person record.
     *
     * @param field
     *            field to return
     * @return the field selected
     */
    private SingleValueRenderableMetadata getField(Field field) {
        String key = metadataType.getName() + '.' + field.toString();
        return (SingleValueRenderableMetadata) members.get(key);
    }

    /**
     * Returns the field type from the fictitious metadata type.
     *
     * @param fictitiousType
     *            fictitious metadata type
     * @return the field to be edited
     */
    static Field getPersonField(String fictitiousType) {
        Matcher matcher = FICTITIOUS_METADATA_TYPE_SCHEME.matcher(fictitiousType);
        if (matcher.matches()) {
            return Field.valueOf(matcher.group(2));
        }
        return null;
    }

    /**
     * Returns the person metadata type name from the fictitious metadata type.
     *
     * @param fictitiousType
     *            fictitious metadata type
     * @return the person to be edited
     */
    static String getPersonType(String fictitiousType) {
        Matcher matcher = FICTITIOUS_METADATA_TYPE_SCHEME.matcher(fictitiousType);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Returns the value of this person as metadata element.
     *
     * @return a list with one person element with the value of this component
     * @see RenderableGroupableMetadata#toMetadata()
     */
    @Override
    public List<PersonInterface> toMetadata() {
        PersonInterface person;
        throw new UnsupportedOperationException("Dead code pending removal");
    }
}
