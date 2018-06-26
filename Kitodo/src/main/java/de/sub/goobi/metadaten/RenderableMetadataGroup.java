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

package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.model.SelectItem;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.exceptions.MetadataException;
import org.kitodo.legacy.UghImplementation;

/**
 * Backing bean for a set of backing beans for input elements to edit a metadata
 * group, with the ability to switch the type of metadata group under edit. It
 * provides the currently selected type of metadata group to add, a list of all
 * types to choose from and the members of the chosen type in order to browse
 * and alter their values.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableMetadataGroup extends RenderableMetadatum {

    /**
     * Holds backing beans for the metadata types that are part of the currently
     * selected metadata type. The backing beans are held as values in this map
     * with their respective metadata type names as keys.
     */
    protected Map<String, RenderableGroupableMetadatum> members = Collections.emptyMap();

    /**
     * Holds all metadata group types that still can be added to the logical
     * document structure node currently selected in the metadata editor. The
     * metadata group types are held as values in this map with their respective
     * metadata group type names as keys. This is done for easily retrieving the
     * corresponding metadata group type when {@link #setType(String)} is called
     * to circumvent the need to iterate over all available types and compare
     * all of their type name strings against the identifier value to set.
     */
    private final Map<String, MetadataGroupTypeInterface> possibleTypes;

    /**
     * Holds the type of metadata group currently under edit in the form backed
     * by this RenderableMetadataGroup instance.
     */
    private MetadataGroupTypeInterface type;

    /**
     * Holds the name of the project the act currently under edit in the
     * metadata editor belongs to. The project name is needed by backing beans
     * for select inputs to retrieve their available options.
     */
    private final String projectName;

    /**
     * Holds the metadata group data instance represented by this backing bean
     * in case that this backing bean represents a metadata group that has
     * already been added to a logical document structure node. It is needed to
     * delete the metadata group from the logical document structure node if the
     * user demands it. If this RenderableMetadataGroup represents a metadata
     * group which is currently created and has not yet been added to a logical
     * document structure node, the field metadataGroup is null.
     */
    private final MetadataGroupInterface metadataGroup;

    /**
     * Metadata editor instance this RenderableMetadataGroup is showing in. It
     * is needed to forward list operations, like deleting the metadata group
     * represented by this instance from the logical document structure node.
     */
    private final Metadaten container;

    /**
     * Creates a RenderableMetadataGroup instance able to add any metadata group
     * that still can be added to the currently selected level of the document
     * structure hierarchy.
     *
     * @param addableTypes
     *            all metadata group types available for adding to the current
     *            logical document structure node
     * @param projectName
     *            project that the act whose metadata group is to edit belongs
     *            to
     * @throws ConfigurationException
     *             if a single value metadata field is configured to show a
     *             multi-select input
     */
    public RenderableMetadataGroup(Collection<MetadataGroupTypeInterface> addableTypes, String projectName)
            throws ConfigurationException {
        super(addableTypes.iterator().next().getAllLanguages(), null);
        possibleTypes = new LinkedHashMap<>(hashCapacityFor(addableTypes));
        for (MetadataGroupTypeInterface possibleType : addableTypes) {
            possibleTypes.put(possibleType.getName(), possibleType);
        }
        type = addableTypes.iterator().next();
        this.projectName = projectName;
        this.metadataGroup = null;
        this.container = null;
        updateMembers(type);
    }

    /**
     * Creates a new RenderableMetadataGroup instance to display and modify a
     * metadata group that has already been assigned to a logical document
     * structure node. This constructor configures the backing beans for the
     * input elements that represent the fields of the metadata group to
     * automatically update the linked metadata group at the moment their
     * setters are called and provides the ability to delete the associated
     * metadata group from the document structure node. Changing the metadata
     * group type is not possible.
     *
     * @param data
     *            metadata group whose data shall be shown
     * @param container
     *            metadata editor instance this RenderableMetadataGroup is
     *            showing in
     * @param language
     *            display language to use
     * @param projectName
     *            project that the process whose metadata group is to edit
     *            belongs to
     * @throws ConfigurationException
     *             if a single value metadata field is configured to show a
     *             multi-select input
     */
    public RenderableMetadataGroup(MetadataGroupInterface data, Metadaten container, String language,
            String projectName) throws ConfigurationException {
        super(data.getMetadataGroupType().getAllLanguages(), data);
        this.possibleTypes = Collections.emptyMap();
        this.type = data.getMetadataGroupType();
        this.projectName = projectName;
        this.metadataGroup = data;
        this.container = container;
        createMembers(data, true);
        setLanguage(language);
    }

    /**
     * Protected constructor for classes extending RenderableMetadataGroup,
     * creates a new RenderableMetadataGroup with exactly one type.
     *
     * @param metadataType
     *            metadata type this element is for
     * @param binding
     *            a metadata group whose value(s) shall be read and updated if
     *            as the getters and setters for the bean are called
     * @param container
     *            metedata group this element belongs to
     * @param type
     *            group type of the element to create
     * @param projectName
     *            name of the project the act belongs to
     * @throws ConfigurationException
     *             if a single value metadata field is configured to show a
     *             multi-select input
     */
    protected RenderableMetadataGroup(MetadataTypeInterface metadataType, MetadataGroupInterface binding,
            RenderableMetadataGroup container, MetadataGroupTypeInterface type, String projectName)
            throws ConfigurationException {

        super(metadataType, binding, container);
        possibleTypes = Collections.emptyMap();
        this.type = type;
        this.projectName = projectName;
        this.metadataGroup = null;
        this.container = null;
        updateMembers(type);
    }

    /**
     * Copy constructor, creates a RenderableMetadataGroup initialised with the
     * data from an existing metadata group, but still able to add any metadata
     * group that still can be added to the currently selected level of the
     * document structure hierarchy. Initialises the type to the type of the
     * copy master and sets the values of the fields to the values of the copy
     * master, but doesn’t bind them to the copy master, so changing the values
     * late won’t arm the master object this copy is derived from.
     *
     * @param master
     *            a metadata group that a copy shall be created of
     * @param addableTypes
     *            all metadata group types that still can be added to the
     *            logical document hierarchy node currently under edit
     */
    public RenderableMetadataGroup(RenderableMetadataGroup master,
            Collection<MetadataGroupTypeInterface> addableTypes) {
        super(master.labels, null);
        possibleTypes = new LinkedHashMap<>(hashCapacityFor(addableTypes));
        for (MetadataGroupTypeInterface possibleType : addableTypes) {
            possibleTypes.put(possibleType.getName(), possibleType);
        }
        type = master.type;
        this.projectName = master.projectName;
        this.metadataGroup = null;
        this.container = null;
        try {
            createMembers(master.toMetadataGroup(), false);
        } catch (ConfigurationException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }

    /**
     * Creates the members for the metadata group. In update mode, the members
     * will be bound tho the metadata group they have been formed from and will
     * automatically intialise themselves from it and update it on every change.
     * If update is false, they need to be initialised explicitly so that they
     * carry a copy of the value. They will not be bound the data object and
     * thus can be used to create a copy of the data.
     *
     * @param data
     *            metadata group whose data shall be shown
     * @param autoUpdate
     *            whether the data structure shall be updated if the member is
     *            edited or not
     * @throws ConfigurationException
     *             if a single value metadata field is configured to show a
     *             multi-select input
     */
    private void createMembers(MetadataGroupInterface data, boolean autoUpdate) throws ConfigurationException {
        List<MetadataTypeInterface> requiredFields = data.getMetadataGroupType().getMetadataTypeList();
        members = new LinkedHashMap<>(hashCapacityFor(requiredFields));
        for (MetadataTypeInterface field : requiredFields) {
            RenderableGroupableMetadatum member;
            if (!(this instanceof RenderablePersonMetadataGroup)) {
                member = RenderableMetadatum.create(field, autoUpdate ? binding : null, this, projectName);
            } else {
                member = new RenderableEdit(field, autoUpdate ? binding : null, this);
            }
            members.put(field.getName(), member);
        }
        if (!autoUpdate) {
            for (MetadataInterface contentValue : data.getMetadataList()) {
                members.get(contentValue.getMetadataType().getName()).addContent(contentValue);
            }
            for (PersonInterface contentValue : data.getPersonList()) {
                members.get(contentValue.getMetadataType().getName()).addContent(contentValue);
            }
        }
    }

    /**
     * Invokes the metadata editor to show the subpage to add a new metadata
     * group, initialised with the values from this instance in order to create
     * a copy of this instance.
     * <p/>
     * This method is provided for the reason that action methods on repeatable
     * elements must be implemented as parameterless methods on the backing
     * beans of the respective list items in JSF. This method just forwards the
     * function call to the component owning the list, which is the more
     * convenient way to implement add or remove operations on lists.
     */
    public void copy() {
        container.showAddMetadataGroupAsCopy(this);
    }

    /**
     * Invokes the metadata editor to delete the metadata group under edit in
     * this instance from the logical document structure node currently under
     * edit.
     * <p/>
     * This method is provided for the reason that action methods on repeatable
     * elements must be implemented as parameterless methods on the backing
     * beans of the respective list items in JSF. This method just forwards the
     * function call to the component owning the list, which is the more
     * convenient way to implement add or remove operations on lists.
     */
    public void delete() {
        container.removeMetadataGroupFromCurrentDocStruct(metadataGroup);
    }

    /**
     * The function getMembers returns the input elements of this metadata
     * group.
     *
     * @return the input elements of this group
     */
    public Collection<RenderableGroupableMetadatum> getMembers() {
        return members.values();
    }

    /**
     * Returns the number of elements in the members list, to be used for the
     * label cell height in HTML.
     *
     * @return the number of elements in the members list.
     */
    public String getRowspan() {
        int result = 0;
        for (RenderableGroupableMetadatum member : members.values()) {
            if (member instanceof RenderableMetadataGroup) {
                result += Integer.parseInt(((RenderableMetadataGroup) member).getRowspan());
            } else {
                result += 1;
            }
        }
        return Integer.toString(result);
    }

    /**
     * The function getPossibleTypes() returns the list of metadata group types
     * available for the currently selected document structure element.
     * Depending on the rule set, availability means that some elements cannot
     * be added more than once and thus may not be available to add any more.
     *
     * @return the metadata group types available
     */
    public Collection<SelectItem> getPossibleTypes() {
        ArrayList<SelectItem> result = new ArrayList<>(possibleTypes.size());
        for (Entry<String, MetadataGroupTypeInterface> possibleType : possibleTypes.entrySet()) {
            result.add(new SelectItem(possibleType.getKey(), possibleType.getValue().getLanguage(language)));
        }
        return result;
    }

    /**
     * The function getSize() returns the number of elements in this metadata
     * group.
     *
     * @return the number of elements in this group
     */
    public int getSize() {
        return members.size();
    }

    /**
     * Returns the internal name of the metadata group type currently under edit
     * to JSF so that it can mark the appropriate option as selected in the
     * metadata group type select box. The user will be shown the label returned
     * for the corresponding element in getPossibleTypes(), not the internal
     * name.
     *
     * @return the internal name of the metadata group type
     */
    public String getType() {
        return type.getName();
    }

    /**
     * Returns whether another instance of the metadata group type under edit in
     * this instance can be created on the logical document structure node
     * currently under edit in the metadata editor to either render the action
     * link to copy this metadata group, or not.
     *
     * @return the internal name of the metadata group type
     */
    public boolean isCopyable() {
        return container != null && container.canCreate(type);
    }

    /**
     * The procedure setLanguage() extends the setter function from
     * RenderableMetadatum because if setLanguage() is called for a metadata
     * group, both the label display language for the group and for all of its
     * members must be set.
     *
     * @see de.sub.goobi.metadaten.RenderableMetadatum#setLanguage(java.lang.String)
     */
    @Override
    void setLanguage(String language) {
        super.setLanguage(language);
        for (RenderableGroupableMetadatum member : members.values()) {
            ((RenderableMetadatum) member).setLanguage(language);
        }
    }

    /**
     * The procedure setType() will be called by JSF to pass back in the
     * metadata group type the user chose to edit, referenced by its name. If it
     * differs from the current one, this renderable metadata group will be
     * updated to represent the new type instead.
     *
     * @param type
     *            name of the metadata group type desired
     * @throws ConfigurationException
     *             if a metadata field designed for a single value is
     *             misconfigured to show a multi-value input element
     */
    public void setType(String type) throws ConfigurationException {
        if (possibleTypes.isEmpty()) {
            return;
        }
        MetadataGroupTypeInterface newType = possibleTypes.get(type);
        if (!newType.equals(this.type)) {
            updateMembers(newType);
        }
        this.type = newType;
    }

    /**
     * Returs the currently showing metadata group as a
     * {@link ugh.dl.MetadataGroup} so that it can be added to some structural
     * element.
     *
     * @return the showing metatdata group as ugh.dl.MetadataGroup
     */
    public MetadataGroupInterface toMetadataGroup() {
        MetadataGroupInterface result;
        try {
            result = UghImplementation.INSTANCE.createMetadataGroup(type);
        } catch (MetadataTypeNotAllowedException e) {
            throw new NullPointerException("MetadataGroupType must not be null at MetadataGroup creation.");
        }
        result.getMetadataList().clear();
        result.getPersonList().clear();

        for (RenderableGroupableMetadatum member : members.values()) {
            for (MetadataInterface element : member.toMetadata()) {
                if (member instanceof RenderablePersonMetadataGroup) {
                    result.addPerson(((PersonInterface) element));
                } else {
                    result.addMetadata(element);
                }
            }

        }
        return result;
    }

    /**
     * The procedure updateMembers() creates or updates the members of this
     * metadata group initially in the constructor and subsequently if the user
     * alters the metadata group type he or she wants to create. Members that
     * previously existed will be kept.
     *
     * @param newGroupType
     *            metadata group type to initialize this renderable metadata
     *            group to
     * @throws ConfigurationException
     *             if a metadata field designed for a single value is
     *             misconfigured to show a multi-value input element
     */
    private void updateMembers(MetadataGroupTypeInterface newGroupType) throws ConfigurationException {
        List<MetadataTypeInterface> requiredMetadataTypes = newGroupType.getMetadataTypeList();
        Map<String, RenderableGroupableMetadatum> newMembers = new LinkedHashMap<>(
                hashCapacityFor(requiredMetadataTypes));
        for (MetadataTypeInterface type : requiredMetadataTypes) {
            RenderableGroupableMetadatum member = members.get(type.getName());
            if (member == null) {
                if (!(this instanceof RenderablePersonMetadataGroup)) {
                    member = RenderableMetadatum.create(type, binding, this, projectName);
                } else {
                    member = new RenderableEdit(type, binding, this);
                }
            }
            newMembers.put(type.getName(), member);
        }
        members = newMembers;
    }

    private static int hashCapacityFor(Collection<?> collection) {
        return (int) Math.ceil(collection.size() / 0.75);
    }
}
