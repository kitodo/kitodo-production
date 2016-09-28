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

package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.ArrayList;

class ConfigOpacDoctype {
	private final String title;
	private final boolean periodical;
	private final boolean multiVolume;
	private final boolean containedWork;
	private final ArrayList<String> mappings;

	ConfigOpacDoctype(String inTitle, boolean periodical, boolean multiVolume, boolean containedWork,
			ArrayList<String> mappings) {
		this.title = inTitle;
		this.periodical = periodical;
		this.multiVolume = multiVolume;
		this.containedWork = containedWork;
		this.mappings = mappings;
	}

	public String getTitle() {
		return this.title;
	}

	boolean isPeriodical() {
		return periodical;
	}

	boolean isMultiVolume() {
		return multiVolume;
	}

	boolean isContainedWork() {
		return containedWork;
	}

	ArrayList<String> getMappings() {
		return mappings;
	}

}
