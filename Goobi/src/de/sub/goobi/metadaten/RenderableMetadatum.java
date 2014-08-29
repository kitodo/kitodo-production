package de.sub.goobi.metadaten;

import java.util.Collection;
import java.util.HashMap;

import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.helper.ConfigDispayRules;

import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;

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
	protected HashMap<String, String> labels;
	protected String language;
	protected boolean readonly = false;

	/**
	 * Creates a renderable metadatum which is not held in a renderable metadata
	 * group. This constructor must be used by all successors that do not
	 * implement RenderableGroupableMetadatum.
	 */
	protected RenderableMetadatum() {
	}

	/**
	 * Creates a renderable metadatum which held in a renderable metadata group.
	 * This constructor must be used by all successors that implement
	 * RenderableGroupableMetadatum.
	 * 
	 * @param container
	 *            group that the renderable metadatum is in
	 */
	protected RenderableMetadatum(RenderableMetadataGroup container) {
		this.container = container;
	}

	/**
	 * Factory method to create a backing bean to render a metadata group.
	 * 
	 * @param projectName
	 * @param bindState
	 * 
	 * @param metadataType
	 *            type of metadatum to create a bean for
	 * @param renderableMetadataGroup
	 *            container that the metadatum is in, may be null if it isn’t in
	 *            a container
	 * @return a backing bean to render the metadatum
	 */
	public static RenderableMetadataGroup create(Collection<MetadataGroupType> elements, String projectName,
			BindState bindState) {
		return new RenderableMetadataGroup(elements, projectName, bindState);
	}

	/**
	 * Factory method to create a backing bean to render a metadatum. Depending
	 * on the configuration, different input component beans will be created.
	 * 
	 * @param metadataType
	 *            type of metadatum to create a bean for
	 * @param renderableMetadataGroup
	 *            container that the metadatum is in, may be null if it isn’t in
	 *            a container
	 * @param projectName
	 * @param bindState
	 * @return a backing bean to render the metadatum
	 */
	public static RenderableGroupableMetadatum create(MetadataType metadataType,
			RenderableMetadataGroup renderableMetadataGroup, String projectName, BindState bindState) {
		if (metadataType.getIsPerson()) {
			return new RenderablePersonMetadataGroup(metadataType, renderableMetadataGroup, projectName, bindState);
		}

		switch (ConfigDispayRules.getInstance().getElementTypeByName(projectName, bindState.getTitle(),
				metadataType.getName())) {
		case input:
			return new RenderableEdit(metadataType, renderableMetadataGroup);
		case readonly:
			return new RenderableBevel(metadataType, renderableMetadataGroup);
		case select:
			return new RenderableListBox(metadataType, renderableMetadataGroup, projectName, bindState);
		case select1:
			return new RenderableDropDownList(metadataType, renderableMetadataGroup, projectName, bindState);
		case textarea:
			return new RenderableLineEdit(metadataType, renderableMetadataGroup);
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
}
