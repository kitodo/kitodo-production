/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
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

import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.exceptions.MetadataTypeNotAllowedException;
import de.sub.goobi.helper.Util;

/**
 * A RenderableMetadataGroup is a RenderableMetadatum which holds several
 * metadata fields as a group. It is a java bean backing a JSF form to add a
 * metadata group. It provides the currently selected type of metadata group to
 * add, a list of all types to choose from and the members of the chosen type in
 * order to browse and alter their values.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class RenderableMetadataGroup extends RenderableMetadatum {

	protected Map<String, RenderableGroupableMetadatum> members = Collections.emptyMap();
	private final Map<String, MetadataGroupType> possibleTypes;
	private MetadataGroupType type;
	private final String projectName;
	private final MetadataGroup metadataGroup;
	private final Metadaten container;

	/**
	 * Creates a new RenderableMetadataGroup.
	 * 
	 * @param addableTypes
	 *            metadata group types available
	 * @param projectName
	 *            project that the process whose metadata group is to edit
	 *            belongs to
	 * @throws ConfigurationException
	 *             if a single value metadata field is configured to show a
	 *             multi-select input
	 */
	public RenderableMetadataGroup(Collection<MetadataGroupType> addableTypes, String projectName)
			throws ConfigurationException {
		super(addableTypes.iterator().next().getAllLanguages(), null);
		possibleTypes = new LinkedHashMap<String, MetadataGroupType>(Util.mapCapacityFor(addableTypes));
		for (MetadataGroupType possibleType : addableTypes) {
			possibleTypes.put(possibleType.getName(), possibleType);
		}
		type = addableTypes.iterator().next();
		this.projectName = projectName;
		this.metadataGroup = null;
		this.container = null;
		updateMembers(type);
	}

	/**
	 * Creates a new RenderableMetadataGroup, populated with the data from the
	 * given metadata group.
	 * 
	 * @param data
	 *            metadata group whose data shall be shown
	 * @param metadaten
	 * @param language
	 *            display language to use
	 * @param projectName
	 *            project that the process whose metadata group is to edit
	 *            belongs to
	 * @throws ConfigurationException
	 *             if a single value metadata field is configured to show a
	 *             multi-select input
	 */
	public RenderableMetadataGroup(MetadataGroup data, Metadaten container, String language, String projectName)
			throws ConfigurationException {
		super(data.getType().getAllLanguages(), data);
		this.possibleTypes = Collections.emptyMap();
		this.type = data.getType();
		this.projectName = projectName;
		this.metadataGroup = data;
		this.container = container;
		createMembers(data);
		setLanguage(language);
	}

	/**
	 * Creates a new RenderableMetadataGroup with exactly one type only.
	 * 
	 * @param metadataType
	 *            metadata type this element is for
	 * @param container
	 *            metedata group this element belongs to
	 * @param type
	 *            group type of the element to create
	 * @param projectName
	 *            name of the project the act belongs to
	 * @param bindState
	 *            whether the metadata group is about to be created or edited
	 * @throws ConfigurationException
	 *             if a single value metadata field is configured to show a
	 *             multi-select input
	 */
	protected RenderableMetadataGroup(MetadataType metadataType, MetadataGroup binding,
			RenderableMetadataGroup container, MetadataGroupType type, String projectName)
			throws ConfigurationException {
		super(metadataType, binding, container);
		possibleTypes = Collections.emptyMap();
		this.type = type;
		this.projectName = projectName;
		this.metadataGroup = null;
		this.container = null;
		updateMembers(type);
	}

	public RenderableMetadataGroup(RenderableMetadataGroup master, List<MetadataGroupType> addableTypes) {
		super(master.labels, null);
		possibleTypes = new LinkedHashMap<String, MetadataGroupType>(Util.mapCapacityFor(addableTypes));
		for (MetadataGroupType possibleType : addableTypes) {
			possibleTypes.put(possibleType.getName(), possibleType);
		}
		type = master.type;
		this.projectName = master.projectName;
		this.metadataGroup = null;
		this.container = null;
		try {
			createMembers(master.toMetadataGroup());
		} catch (ConfigurationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Creates the members for the metadata group.
	 * 
	 * @param data
	 *            metadata group whose data shall be shown
	 * @throws ConfigurationException
	 *             if a single value metadata field is configured to show a
	 *             multi-select input
	 */
	private final void createMembers(MetadataGroup data) throws ConfigurationException {
		List<MetadataType> requiredFields = data.getType().getMetadataTypeList();
		members = new LinkedHashMap<String, RenderableGroupableMetadatum>(Util.mapCapacityFor(requiredFields));
		for (MetadataType createField : requiredFields) {
			RenderableGroupableMetadatum member;
			if (!(this instanceof RenderablePersonMetadataGroup)) {
				member = RenderableMetadatum.create(createField, binding, this, projectName);
			} else {
				member = new RenderableEdit(createField, binding, this);
			}
			members.put(createField.getName(), member);
		}
		for (Metadata contentValue : data.getMetadataList()) {
			members.get(contentValue.getType().getName()).addContent(contentValue);
		}
		for (Person contentValue : data.getPersonList()) {
			members.get(contentValue.getType().getName()).addContent(contentValue);
		}
	}

	public void copy() {
		container.showAddMetadataGroupAsCopy(this);
	}

	/**
	 * Deletes this metadata group.
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
		ArrayList<SelectItem> result = new ArrayList<SelectItem>(possibleTypes.size());
		for (Entry<String, MetadataGroupType> possibleType : possibleTypes.entrySet()) {
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
	 */
	public void setType(String type) throws ConfigurationException {
		if (possibleTypes.isEmpty()) {
			return;
		}
		MetadataGroupType newType = possibleTypes.get(type);
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
	public MetadataGroup toMetadataGroup() {
		MetadataGroup result;
		try {
			result = new MetadataGroup(type);
		} catch (MetadataTypeNotAllowedException e) {
			throw new NullPointerException("MetadataGroupType must not be null at MetadataGroup creation.");
		}
		result.getMetadataList().clear();

		for (RenderableGroupableMetadatum member : members.values()) {
			for (Metadata element : member.toMetadata()) {
				if (member instanceof RenderablePersonMetadataGroup) {
					result.addPerson(((Person) element));
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
	 */
	private final void updateMembers(MetadataGroupType newGroupType) throws ConfigurationException {
		List<MetadataType> requiredMetadataTypes = newGroupType.getMetadataTypeList();
		Map<String, RenderableGroupableMetadatum> newMembers = new LinkedHashMap<String, RenderableGroupableMetadatum>(
				Util.mapCapacityFor(requiredMetadataTypes));
		for (MetadataType type : requiredMetadataTypes) {
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
}
