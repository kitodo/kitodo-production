package de.sub.goobi.metadaten.copier;

import java.lang.reflect.Field;

/**
 * A VariableSelector provides methods to retrieve variables used in Goobi.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class VariableSelector extends DataSelector {

	/**
	 * Holds the name of the variable to resolve
	 */
	private final String qualifier;

	/**
	 * If the selector passed to the constructor references a variable that is
	 * part of an object which is held in a variable itself, then this
	 * VariableSelector only handles the resolving of the first variable, and
	 * the field subselector holds another VariableSelector to resolve the
	 * remaining variable. Otherwise, this field is null.
	 */
	private final VariableSelector subselector;

	/**
	 * Creates a new VariableSelector.
	 * 
	 * @param selector
	 *            String identifying a variable
	 */
	public VariableSelector(String selector) {
		if (selector.startsWith(VARIABLE_REFERENCE)) {
			selector = selector.substring(1);
		}
		String[] a = selector.split("\\.", 2);
		if (a.length == 2) {
			this.qualifier = a[0];
			this.subselector = new VariableSelector(a[1]);
		} else {
			this.qualifier = selector;
			this.subselector = null;
		}
	}

	/**
	 * Returns the value of the variable named by the path used to construct the
	 * variable selector. Returns null if the variable isn’t available.
	 * 
	 * @param data
	 *            object to inspect
	 * @return value of the variable, or null if not found
	 * @see de.sub.goobi.metadaten.DataSelector#findIn(ugh.dl.DocStruct)
	 */
	@Override
	public String findIn(CopierData data) {
		return findIn((Object) data);
	}

	/**
	 * Returns the value of the variable named by the path used to construct the
	 * variable selector. Returns null if the variable isn’t available.
	 * 
	 * @param obj
	 *            object to inspect
	 * @return value of the variable, or null if not found
	 */
	private String findIn(Object obj) {
		try {
			Field variable = obj.getClass().getDeclaredField(qualifier);
			variable.setAccessible(true);
			if (subselector == null) {
				return String.valueOf(variable);
			} else {
				return subselector.findIn(variable);
			}
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	/**
	 * Returns a string that textually represents this LocalMetadataSelector.
	 * 
	 * @return a string representation of this LocalMetadataSelector
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (subselector == null) {
			return VARIABLE_REFERENCE + qualifier;
		} else {
			return VARIABLE_REFERENCE + qualifier + '.' + subselector.toString().substring(1);
		}
	}
}
