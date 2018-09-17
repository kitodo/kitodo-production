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

import de.sub.goobi.metadaten.Metadaten;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.Parameter;
import org.kitodo.helper.Helper;
import org.kitodo.legacy.UghImplementation;

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

        private boolean isIdentifier;
        private String resourceKey;

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
            this.isIdentifier = isIdentifier;
            this.resourceKey = resourceKey;
        }

        /**
         * Returns a key string to look up the translated labels for the field
         * in the messages file.
         *
         * @return key string to look up the labels for the field
         */
        private String getResourceKey() {
            return resourceKey;
        }

        /**
         * Returns whether or not the given field is an identifier.
         *
         * @return whether the given field is an identifier
         */
        private boolean isIdentifier() {
            return isIdentifier;
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
    public PersonMetadataGroup(MetadataTypeInterface metadataType, MetadataGroupInterface binding,
                               RenderableMetadataGroup container, String projectName) throws ConfigurationException {

        super(metadataType, binding, container, getGroupTypeFor(metadataType), projectName);
        checkConfiguration();
        getField(Field.NORMDATA_RECORD).setValue(ConfigCore.getParameter(Parameter.AUTHORITY_DEFAULT, ""));
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
    private static MetadataGroupTypeInterface getGroupTypeFor(MetadataTypeInterface type) {
        MetadataGroupTypeInterface result = UghImplementation.INSTANCE.createMetadataGroupType();
        result.setName(type.getName());
        result.setAllLanguages(type.getAllLanguages());
        if (type.getNum() != null) {
            result.setNum(type.getNum());
        }
        for (Field field : Field.values()) {
            result.addMetadataType(getMetadataTypeFor(type, field));
        }
        return result;
    }

    /**
     * Creates a fictitious MetadataType for the given field of the given
     * metadata type, assuming that the latter is a person. The method is called
     * from the constructor and thus should not be overloaded.
     *
     * @param type
     *            a metadata type which represents a person
     * @param field
     *            a field of the person record
     * @return a fictitious MetadataGroupType with the person’s subfields
     */
    private static MetadataTypeInterface getMetadataTypeFor(MetadataTypeInterface type, Field field) {
        MetadataTypeInterface result = UghImplementation.INSTANCE.createMetadataType();
        result.setName(type.getName() + '.' + field.toString());
        if (type.getNum() != null) {
            result.setNum(type.getNum());
        }
        result.setAllLanguages(Helper.getAllStrings(field.getResourceKey()));
        result.setPerson(false);
        result.setIdentifier(field.isIdentifier());
        return result;
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
     * @see RenderableGroupableMetadata#addContent(ugh.dl.Metadata)
     */
    @Override
    public void addContent(MetadataInterface data) {
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
     * Returns the value of this person as metadata element
     *
     * @return a list with one person element with the value of this component
     * @see RenderableGroupableMetadata#toMetadata()
     */
    @Override
    public List<PersonInterface> toMetadata() {
        List<PersonInterface> result = new ArrayList<>(1);
        PersonInterface person;
        try {
            person = UghImplementation.INSTANCE.createPerson(metadataType);
        } catch (MetadataTypeNotAllowedException e) {
            throw new NullPointerException(e.getMessage());
        }
        String normdataRecord = getField(Field.NORMDATA_RECORD).getValue();
        if (normdataRecord != null && normdataRecord.length() > 0
                && !normdataRecord.equals(ConfigCore.getParameter(Parameter.AUTHORITY_DEFAULT, ""))) {
            String[] authorityFile = Metadaten.parseAuthorityFileArgs(normdataRecord);
            person.setAutorityFile(authorityFile[0], authorityFile[1], authorityFile[2]);
        }
        person.setFirstName(getField(Field.FIRSTNAME).getValue());
        person.setLastName(getField(Field.LASTNAME).getValue());
        result.add(person);
        return result;
    }
}
