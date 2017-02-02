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

import java.util.*;

/**
 * die OpacBeautifier dienen zur Manipulation des Ergebnisses, was als Treffer
 * einer Opacabfrage zurückgegeben wird. Dabei soll die Eigenschaft eines Wertes
 * gesetzt werden, wenn bestimmte Werte in dem opac-Ergebnis auftreten.
 */
class Setvalue {
	private final String tag;
	private final String subtag;
	private final String value;
	private final String mode;
	private final List<Condition> conditions;

	Setvalue(String tag, String subtag, String value, String mode,
			List<Condition> conditions) {
		this.tag = tag;
		this.subtag = subtag;
		this.value = value;
		this.mode = mode;		
		this.conditions = conditions;
	}

	String getTag() {
		return tag;
	}

	String getSubtag() {
		return subtag;
	}

	String getValue() {
		return value;
	}

	String getMode() {
		return mode;
	}
	
	List<Condition> getConditions() {
		return conditions;
	}
}
