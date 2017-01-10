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

package org.goobi.production.properties;

public enum AccessCondition {
	READ, WRITE, WRITEREQUIRED;

	/**
	 * @param inName add description
	 * @return add description
	 */
	public static AccessCondition getAccessConditionByName(String inName) {
		if (inName.equalsIgnoreCase("write")) {
			return WRITE;
		}
		if (inName.equalsIgnoreCase("writerequired")) {
			return WRITEREQUIRED;
		}
		return READ;
	}
}
