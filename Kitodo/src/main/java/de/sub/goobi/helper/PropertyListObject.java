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

package de.sub.goobi.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.goobi.production.properties.ProcessProperty;

public class PropertyListObject implements Serializable {

	private static final long serialVersionUID = 1119130003588038047L;

	private List<ProcessProperty> propertyList = new ArrayList<ProcessProperty>();
	private int containerNumber = 0;

	public PropertyListObject() {
	}

	public PropertyListObject(int container) {
		this.containerNumber = container;
	}

	public void addToList(ProcessProperty pp) {
		this.propertyList.add(pp);
	}

	public int getContainerNumber() {
		return this.containerNumber;
	}

	public List<ProcessProperty> getPropertyList() {
		return this.propertyList;
	}

	public int getPropertyListSize() {
		return this.propertyList.size();
	}

	public String getPropertyListSizeString() {
		return "" + this.propertyList.size();
	}
}
