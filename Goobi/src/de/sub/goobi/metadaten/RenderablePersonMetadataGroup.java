package de.sub.goobi.metadaten;

import java.util.Arrays;
import java.util.Collection;

import javax.faces.model.SelectItem;

import org.goobi.api.display.enums.BindState;

import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import de.sub.goobi.helper.Helper;

public class RenderablePersonMetadataGroup extends RenderableMetadataGroup implements RenderableGroupableMetadatum {

	/**
	 * The enum Field holds the fields to show in a
	 * RenderablePersonMetadataGroup.
	 * 
	 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
	 */
	enum Field {
		NORMDATA_RECORD("normDataRecord", true), FIRSTNAME("vorname", false), LASTNAME("nachname", false);

		private boolean isIdentifier;
		private String resourceKey;

		Field(String resourceKey, boolean isIdentifier) {
			this.resourceKey = resourceKey;
			this.isIdentifier = isIdentifier;
		}

		private String getResourceKey() {
			return resourceKey;
		}

		private boolean isIdentifier() {
			return isIdentifier;
		}
	};

	public RenderablePersonMetadataGroup(MetadataType metadataType, RenderableMetadataGroup renderableMetadataGroup,
			String projectName, BindState bindState) {
		super(Arrays.asList(new MetadataGroupType[] { getGroupTypeFor(metadataType) }), projectName, bindState);
		super.labels = metadataType.getAllLanguages();
	}

	private static final MetadataGroupType getGroupTypeFor(MetadataType type) {
		MetadataGroupType result = new MetadataGroupType();
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

	private static final MetadataType getMetadataTypeFor(MetadataType type, Field field) {
		MetadataType result = new MetadataType();
		result.setName(type.getName() + '.' + field.toString());
		if (type.getNum() != null) {
			result.setNum(type.getNum());
		}
		result.setAllLanguages(Helper.getAllStrings(field.getResourceKey()));
		result.setIsPerson(false);
		result.setIdentifier(field.isIdentifier());
		return result;
	}

	@Override
	public Collection<SelectItem> getItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> getSelectedItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSelectedItems(Collection<String> selectedItems) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}

}
