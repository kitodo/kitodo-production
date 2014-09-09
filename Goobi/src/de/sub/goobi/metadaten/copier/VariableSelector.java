/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
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
