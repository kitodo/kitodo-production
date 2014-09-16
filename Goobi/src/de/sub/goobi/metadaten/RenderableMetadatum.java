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

import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.helper.ConfigDispayRules;

import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.MetadataTypeNotAllowedException;

import com.sharkysoft.util.UnreachableCodeException;

/**
 * A RenderableMetadatum is a java bean that is backing an input element
 * renderable by JSF to allow showing and editing a metadatum. This may be a
 * RenderableMetadataGroup or a class implementing RenderableGroupableMetadatum,
 * where the latter can—but doesn’t have to be— a member of a
 * RenderableMetadataGroup. A RenderableMetadataGroup cannot be a member of a
 * RenderableMetadataGroup itself, whereas a RenderablePersonMetadataGroup,
 * which is a special case of a RenderableMetadataGroup, can.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public abstract class RenderableMetadatum {

	private RenderableMetadataGroup container = null;
	protected String language;
	protected boolean readonly = false;
	protected final MetadataType metadataType;
	public final Map<String, String> labels;

	/**
	 * Creates a renderable metadatum which is not held in a renderable metadata
	 * group. A label isn’t needed in this case. This constructor must be used
	 * by all successors that do not implement RenderableGroupableMetadatum.
	 */
	protected RenderableMetadatum(Map<String, String> labels) {
		metadataType = null;
		this.labels = labels;
	}

	/**
	 * Creates a renderable metadatum which held in a renderable metadata group.
	 * This constructor must be used by all successors that implement
	 * RenderableGroupableMetadatum.
	 * 
	 * @param metadataType
	 *            metadata type represented by this input element
	 * @param container
	 *            group that the renderable metadatum is in
	 */
	protected RenderableMetadatum(MetadataType metadataType, RenderableMetadataGroup container) {
		this.metadataType = metadataType;
		this.labels = metadataType.getAllLanguages();
		this.container = container;
	}

	/**
	 * Factory method to create a backing bean to render a metadatum. Depending
	 * on the configuration, different input component beans will be created.
	 * 
	 * @param metadataType
	 *            type of metadatum to create a bean for
	 * @param container
	 *            container that the metadatum is in, may be null if it isn’t in
	 *            a container
	 * @param projectName
	 *            name of the project the document under edit does belong to
	 * @param bindState
	 *            whether the metadatum is being created or edited
	 * @return a backing bean to render the metadatum
	 * @throws ConfigurationException
	 *             if a metadata field designed for a single value is
	 *             misconfigured to show a multi-value input element
	 */
	public static RenderableGroupableMetadatum create(MetadataType metadataType, RenderableMetadataGroup container,
			String projectName, BindState bindState) throws ConfigurationException {
		if (metadataType.getIsPerson()) {
			return new RenderablePersonMetadataGroup(metadataType, container, projectName, bindState);
		}
		switch (ConfigDispayRules.getInstance().getElementTypeByName(projectName, bindState.getTitle(),
				metadataType.getName())) {
		case input:
			return new RenderableEdit(metadataType, container);
		case readonly:
			return new RenderableEdit(metadataType, container).setReadonly(true);
		case select:
			return new RenderableListBox(metadataType, container, projectName, bindState);
		case select1:
			return new RenderableDropDownList(metadataType, container, projectName, bindState);
		case textarea:
			return new RenderableLineEdit(metadataType, container);
		default:
			throw new UnreachableCodeException("Complete switch statement");
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
	protected Metadata getMetadata(String value) {
		Metadata result;
		try {
			result = new Metadata(metadataType);
		} catch (MetadataTypeNotAllowedException e) {
			throw new NullPointerException(e.getMessage());
		}
		result.setValue(value);
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

}
