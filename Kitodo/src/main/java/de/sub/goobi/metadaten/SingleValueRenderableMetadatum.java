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
 * A single value renderable metadatum may reference any renderable metadataum which does only hold one single value.
 * It provides a setValue() and getValue() method.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
interface SingleValueRenderableMetadatum {
	/**
	 * Sets the value of this editing component.
	 *
	 * @param value value to set
	 */
	void setValue(String value);

	/**
	 * May be used to retrieve the value from this editing component.
	 *
	 * @return the value inside the component
	 */
	String getValue();
}
