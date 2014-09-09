package de.sub.goobi.metadaten.copier;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

public class OverwriteOrCreateRule extends DataCopyrule {

	protected static final String OPERATOR = "=";
	private MetadataSelector destination;
	private DataSelector source;

	@Override
	public void apply(CopierData data) {
		String value = source.findIn(data);
		if (value == null) {
			return;
		}
		destination.createOrOverwrite(data, value);
	}

	@Override
	protected int getMinObjects() {
		return 1;
	}

	@Override
	protected int getMaxObjects() {
		return 1;
	}

	/**
	 * Saves the source object path.
	 * 
	 * @see de.sub.goobi.metadaten.MetadataCopyrule#setObjects(java.util.List)
	 */
	@Override
	protected void setObjects(List<String> objects) throws ConfigurationException {
		destination = MetadataSelector.create(objects.get(0));
	}

	/**
	 * Saves the destination object path.
	 * 
	 * @see de.sub.goobi.metadaten.MetadataCopyrule#setSubject(java.lang.String)
	 */
	@Override
	protected void setSubject(String subject) throws ConfigurationException {
		source = DataSelector.create(subject);
	}

	/**
	 * Returns a string that textually represents this copy rule.
	 * 
	 * @return a string representation of this copy rule
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return destination.toString() + ' ' + OPERATOR + ' ' + source.toString();
	}
}
