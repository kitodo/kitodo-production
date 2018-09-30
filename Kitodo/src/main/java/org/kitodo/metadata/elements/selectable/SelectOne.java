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

package org.kitodo.metadata.elements.selectable;

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
     * The available elements.
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
        entries = new LinkedHashMap<>((int) Math.ceil(elements.size() / 0.75));
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
    public T getObject() {
        return selected;
    }

    /**
     * Sets the selected element by its ID.
     * 
     * @param id
     *            ID of the element to select.
     */
    public void setSelected(String id) {
        T found = entries.get(id);
        if (found != null) {
            this.selected = found;
        }
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
