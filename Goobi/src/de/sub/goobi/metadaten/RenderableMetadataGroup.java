package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.model.SelectItem;

import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import de.sub.goobi.helper.Util;

/**
 * An RenderableMetadataGroup is a java bean backing a JSF form to add a
 * metadata group. It provides the currently selected type of metadata group to
 * add, a list of all types to choose from and the members of the chosen type in
 * order to browse and alter their values.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableMetadataGroup extends RenderableMetadatum {

	private Map<String, RenderableGroupedMetadatum> members = Collections.emptyMap();
	private final Map<String, MetadataGroupType> possibleTypes;
	private MetadataGroupType type;

	/**
	 * RenderableMetadataGroup constructor. Creates a new
	 * RenderableMetadataGroup.
	 * 
	 * @param addableTypes
	 *            metadata group types available to add
	 */
	public RenderableMetadataGroup(Collection<MetadataGroupType> addableTypes) {
		possibleTypes = new LinkedHashMap<String, MetadataGroupType>(Util.mapCapacityFor(addableTypes));
		for (MetadataGroupType possibleType : addableTypes) {
			possibleTypes.put(possibleType.getName(), possibleType);
		}
		type = addableTypes.iterator().next();
		updateMembers(type);
	}

	/**
	 * The function getMembers returns the input elements of this metadata
	 * group.
	 * 
	 * @return the input elements of this group
	 */
	public Collection<RenderableGroupedMetadatum> getMembers() {
		return members.values();
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
		ArrayList<SelectItem> result = new ArrayList<SelectItem>(possibleTypes.size());
		for (Entry<String, MetadataGroupType> possibleType : possibleTypes.entrySet()) {
			result.add(new SelectItem(possibleType.getKey(), possibleType.getValue().getLanguage(language)));
		}
		return result;
	}

	/**
	 * The function getType() returns the internal name of the metadata group
	 * type currently under edit to JSF so that it can mark the appropriate
	 * option as selected in the metadata group type select box. The user will
	 * be shown the label returned for the corresponding element in
	 * getPossibleTypes(), not the internal name.
	 * 
	 * @return the internal name of the metadata group type
	 */
	public String getType() {
		return type.getName();
	}

	/**
	 * The procedure setType() will be called by JSF to pass back in the
	 * metadata group type the user chose to edit, referenced by its name. If it
	 * differs from the current one, this renderable metadata group will be
	 * updated to represent the new type instead.
	 * 
	 * @param type
	 *            name of the metadata group type desired
	 */
	public void setType(String type) {
		MetadataGroupType newType = possibleTypes.get(type);
		if (!newType.equals(this.type)) {
			updateMembers(newType);
		}
		this.type = newType;
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
	 */
	private void updateMembers(MetadataGroupType newGroupType) {
		List<MetadataType> requiredMetadataTypes = newGroupType.getMetadataTypeList();
		Map<String, RenderableGroupedMetadatum> newMembers = new LinkedHashMap<String, RenderableGroupedMetadatum>(
				Util.mapCapacityFor(requiredMetadataTypes));
		for (MetadataType type : requiredMetadataTypes) {
			String name = type.getName();
			newMembers.put(name, members.containsKey(name) ? members.get(name) : RenderableMetadatum.create(type));
		}
		members = newMembers;
	}
}
