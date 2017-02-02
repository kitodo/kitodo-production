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

/**
 * die OpacBeautifier dienen zur Manipulation des Ergebnisses, was als Treffer
 * einer Opacabfrage zurückgegeben wird. Dabei soll die Eigenschaft eines Wertes
 * gesetzt werden, wenn bestimmte Werte in dem opac-Ergebnis auftreten.
 */
class ConfigOpacCatalogueBeautifier {
	private final ConfigOpacCatalogueBeautifierElement tagElementToChange;
	private final ArrayList<ConfigOpacCatalogueBeautifierElement> tagElementsToProof;

	ConfigOpacCatalogueBeautifier(ConfigOpacCatalogueBeautifierElement inChangeElement,
			ArrayList<ConfigOpacCatalogueBeautifierElement> inProofElements) {
		this.tagElementToChange = inChangeElement;
		this.tagElementsToProof = inProofElements;
	}

	ConfigOpacCatalogueBeautifierElement getTagElementToChange() {
		return tagElementToChange;
	}

	ArrayList<ConfigOpacCatalogueBeautifierElement> getTagElementsToProof() {
		return tagElementsToProof;
	}
}
