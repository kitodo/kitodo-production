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

package org.goobi.api.display.enums;

public enum BindState {

	create("0", "create"), edit("1", "edit");

	private String id;
	private String title;

	private BindState(String myId, String myTitle) {
		id = myId;
		title = myTitle;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * @param inTitle add description
	 * @return add description
	 */
	public static BindState getByTitle(String inTitle) {
		for (BindState type : BindState.values()) {
			if (type.getTitle().equals(inTitle)) {
				return type;
			}
		}
		return edit; // edit is default
	}

}
