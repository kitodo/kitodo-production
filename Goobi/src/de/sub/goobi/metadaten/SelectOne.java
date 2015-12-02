/*
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 *
 * (c) 2015 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A class of elements that one of can be selected.
 * 
 * @author Matthias Ronge
 *
 * @param <T>
 *            Elements to select from
 */
public class SelectOne<T extends Selectable> {

	/**
	 * The available elements
	 */
	private LinkedHashMap<String, T> entries;

	/**
	 * The currently selected element.
	 */
	private T selected;

	/**
	 * Creates a new select class.
	 * 
	 * @param elements
	 *            selectable elements
	 */
	public SelectOne(Collection<T> elements) {
		entries = new LinkedHashMap<String, T>((int) Math.ceil(elements.size() / 0.75));
		for (T element : elements) {
			entries.put(element.getId(), element);
		}
		selected = entries.entrySet().iterator().next().getValue();
	}

	/**
	 * Returns the selected element’s ID.
	 * 
	 * @return the selected element’s ID
	 */
	public String getSelected() {
		return selected.getId();
	}
	
	/**
	 * Returns the selected object.
	 * 
	 * @return the selected object
	 */
	public T getObject(){
		return selected;
	}

	/**
	 * Sets the selected element by its ID
	 * 
	 * @param id
	 *            ID of the element to select.
	 */
	public void setSelected(String id) {
		T found = entries.get(id);
		if (found != null)
			this.selected = found;
	}

	/**
	 * Returns the elements to choose from.
	 * 
	 * @return elements to choose from
	 */
	public Collection<T> getItems() {
		return entries.values();
	}

}
