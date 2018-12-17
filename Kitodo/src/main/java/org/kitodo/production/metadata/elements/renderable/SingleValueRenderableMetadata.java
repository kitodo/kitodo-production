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

package org.kitodo.production.metadata.elements.renderable;

/**
 * A single value renderable metadata may reference any renderable metadata
 * which does only hold one single value. It provides a setValue() and
 * getValue() method.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
interface SingleValueRenderableMetadata {
    /**
     * Sets the value of this editing component.
     * 
     * @param value
     *            value to set
     */
    void setValue(String value);

    /**
     * May be used to retrieve the value from this editing component.
     * 
     * @return the value inside the component
     */
    String getValue();
}
