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

package org.goobi.pagination;

import java.util.ArrayList;

import ugh.dl.RomanNumeral;

public class RomanNumberSequence extends ArrayList<String> {
	/**
	 * @param start add description
	 * @param end add description
	 */
	public RomanNumberSequence(int start, int end) {

		generateElements(start, end, 1);

	}

	/**
	 * @param start add description
	 * @param end add description
	 * @param increment add description
	 */
	public RomanNumberSequence(int start, int end, int increment) {

		generateElements(start, end, increment);

	}

	private void generateElements(int start, int end, int increment) {
		RomanNumeral r = new RomanNumeral();
		for (int i = start; i <= end; i = (i + increment)) {
			r.setValue(i);
			this.add(r.toString());
		}
	}

}
