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

package de.sub.goobi.metadaten;

/**
 * The interface indicates elements that can be selected in select lists in the
 * JSF front end.
 *
 * @author Matthias Ronge
 */
public interface Selectable {

    /**
     * Return an ID for the HTML {@code <OPTION>} element. Two different
     * elements in the list must have different IDs.
     *
     * @return an ID for the element
     */
    String getId();

    /**
     * Return the human readable text in the HTML {@code <OPTION>} element.
     *
     * @return the label of the elment in the web browser
     */
    String getLabel();
}