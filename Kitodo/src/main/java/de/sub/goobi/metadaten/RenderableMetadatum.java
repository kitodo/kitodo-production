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
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDisplayRules;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.exceptions.UnreachableCodeException;
import org.kitodo.legacy.UghImplementation;

/**
 * Abstract base class for all kinds of backing beans usable to render input
 * elements in JSF to edit a metadatum. This may be a RenderableMetadataGroup or
 * a class implementing RenderableGroupableMetadatum, where the latter can—but
 * doesn’t have to be—a member of a RenderableMetadataGroup. A
 * RenderableMetadataGroup cannot be a member of a RenderableMetadataGroup
 * itself, whereas a RenderablePersonMetadataGroup, which is a special case of a
 * RenderableMetadataGroup, can.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public abstract class RenderableMetadatum {

    /**
     * Holds a reference to the renderable metadata group that this metadata
     * group is in.
     */
    private RenderableMetadataGroup container = null;

    /**
     * Holds the string identifier of the language to use to show the labels of
     * the metadata elements in.
     */
    protected String language;

    /**
     * Indicates whether this metadatum can only be read by the user and not be
     * altered.
     */
    protected boolean readonly = false;

    /**
     * Holds the metadata type represented by this input element.
     */
    protected final MetadataTypeInterface metadataType;

    /**
     * Holds the available labels for this input element.
     */
    public final Map<String, String> labels;

    /**
     * Holds a reference to a metadata group whose value(s) shall be updated if
     * as the setters for the bean are called. May be null if this feature is
     * unused.
     */
    protected final MetadataGroupInterface binding;

    /**
     * Creates a renderable metadatum which is not held in a renderable metadata
     * group. A label isn’t needed in this case. This constructor must be used
     * by all successors that do not implement RenderableGroupableMetadatum.
     *
     * @param labels
     *            available labels for this input element
     * @param binding
     *            a metadata group whose value(s) shall be updated if as the
     *            setters for the bean are called
     */
    protected RenderableMetadatum(Map<String, String> labels, MetadataGroupInterface binding) {
        this.metadataType = null;
        this.labels = labels;
        this.binding = binding;
    }

    /**
     * Creates a renderable metadatum which held in a renderable metadata group.
     * This constructor must be used by all successors that implement
     * RenderableGroupableMetadatum.
     *
     * @param metadataType
     *            metadata type represented by this input element
     * @param binding
     *            a metadata group whose value(s) shall be read and updated if
     *            as the getters and setters for the bean are called
     * @param container
     *            group that the renderable metadatum is in
     */
    protected RenderableMetadatum(MetadataTypeInterface metadataType, MetadataGroupInterface binding,
            RenderableMetadataGroup container) {

        this.metadataType = metadataType;
        this.labels = metadataType.getAllLanguages();
        this.binding = binding;
        this.container = container;
    }

    /**
     * Factory method to create a backing bean to render a metadatum. Depending
     * on the configuration, different input component beans will be created.
     *
     * @param metadataType
     *            type of metadatum to create a bean for
     * @param binding
     *            a metadata group whose value(s) shall be read and updated if
     *            as the getters and setters for the bean are called
     * @param container
     *            container that the metadatum is in, may be null if it isn’t in
     *            a container
     * @param projectName
     *            name of the project the document under edit does belong to
     * @return a backing bean to render the metadatum
     * @throws ConfigurationException
     *             if a metadata field designed for a single value is
     *             misconfigured to show a multi-value input element
     */
    public static RenderableGroupableMetadatum create(MetadataTypeInterface metadataType,
            MetadataGroupInterface binding, RenderableMetadataGroup container, String projectName)
            throws ConfigurationException {

        if (metadataType.isPerson()) {
            return new RenderablePersonMetadataGroup(metadataType, binding, container, projectName);
        }
        switch (ConfigDisplayRules.getInstance().getElementTypeByName(projectName, getBindState(binding),
            metadataType.getName())) {
            case INPUT:
                return new RenderableEdit(metadataType, binding, container);
            case READONLY:
                return new RenderableEdit(metadataType, binding, container).setReadonly(true);
            case SELECT:
                return new RenderableListBox(metadataType, binding, container, projectName);
            case SELECT1:
                return new RenderableDropDownList(metadataType, binding, container, projectName);
            case TEXTAREA:
                return new RenderableLineEdit(metadataType, binding, container);
            default:
                throw new UnreachableCodeException("Complete switch statement");
        }
    }

    /**
     * Returns whether the metadatum represented by this instance is about to be
     * created or under edit.
     *
     * @return whether this metadatum is created or edited
     */
    protected String getBindState() {
        return getBindState(binding);
    }

    /**
     * Returns whether the metadatum whose binding is passed is about to be
     * created or under edit.
     *
     * @param binding
     *            an object to bind to, or null
     * @return whether the metadatum is created or edited
     */
    protected static String getBindState(Object binding) {
        if (binding == null) {
            return BindState.CREATE.getTitle();
        } else {
            return BindState.EDIT.getTitle();
        }
    }

    /**
     * Returns the label of the metadatum in the language previously set. This
     * is a getter method which is automatically called by Faces to resolve the
     * read-only property “label”, thus we cannot pass the language as a
     * parameter here. It must have been set beforehand.
     *
     * @return the translated label of the metadatum
     */
    public String getLabel() {
        return labels.get(language);
    }

    /**
     * Creates and returns a metadatum of the internal type with the value
     * passed in.
     *
     * @param value
     *            value to set the metadatum to
     * @return a metadatum with the value
     */
    protected MetadataInterface getMetadata(String value) {
        MetadataInterface result;
        try {
            result = UghImplementation.INSTANCE.createMetadata(metadataType);
        } catch (MetadataTypeNotAllowedException e) {
            throw new NullPointerException(e.getMessage());
        }
        result.setStringValue(value);
        return result;
    }

    /**
     * Returns true if the metadatum is contained in a metadata group and is the
     * first element in that group. This is to overcome a shortcoming of
     * Tomahawk’s dataList which doesn’t provide a boolean “first” variable to
     * tell whether we are in the first iteration of the loop or not.
     *
     * @return if the metadatum is the first element in its group
     */
    public boolean isFirst() {
        return container != null && container.getMembers().iterator().next().equals(this);
    }

    /**
     * Returns whether the metadatum may not be changed by the user.
     *
     * @return whether the metadatum is read-only
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * Setter method to set the language to return labels in. This will affect
     * both the label for the metadatum and the labels of items in select and
     * listbox elements. Metadata groups have to overload this method to also
     * set the language of their respective members.
     *
     * @param language
     *            language to return the labels in
     */
    void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Can be used do set whether the metadatum may not be changed by the user.
     *
     * @param readolny
     *            whether the metadatum is read-only
     * @return the object itself, to be able to call the setter in line with the
     *         constructor
     */
    protected RenderableGroupableMetadatum setReadonly(boolean readolny) {
        this.readonly = readolny;
        return (RenderableGroupableMetadatum) this;
    }

    /**
     * Updates the bound metadata group by the current value(s) of the
     * implementing instance.
     */
    protected void updateBinding() {
        if (binding != null) {
            List<MetadataInterface> bound = binding.getMetadataList();
            bound.removeAll(binding.getMetadataByType(metadataType.getName()));
            bound.addAll(((RenderableGroupableMetadatum) this).toMetadata());
        }
    }

    /**
     * Returns the available items for select input elements. The available
     * items can vary depending on the project, wheter the metadata element is
     * about to be created or is under edit, the metadata type and the type of
     * the input element.
     * <p/>
     * Since the items hold their selected state and ConfigDispayRules does
     * return the same item instances again if called several times, we need to
     * create a deep copy of the retrieved list here, so that several select
     * lists lists of the same type can hold their individual selected state.
     *
     * @param projectName
     *            project of the process owning this metadatum
     * @param type
     *            type of input element to get the items for
     * @return the collection of available items for the input element
     */
    protected final Collection<Item> getItems(String projectName, DisplayType type) {
        List<Item> prototypes = ConfigDisplayRules.getInstance().getItemsByNameAndType(projectName, getBindState(),
            metadataType.getName(), type);
        ArrayList<Item> result = new ArrayList<>(prototypes.size());
        for (Item item : prototypes) {
            result.add(new Item(item.getLabel(), item.getValue(), item.getIsSelected()));
        }
        return result;
    }

}
