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

import ugh.dl.RomanNumeral;

import java.util.ArrayList;

public class RomanNumberSequence extends ArrayList<String> {

	public RomanNumberSequence(int start, int end) {

		generateElements(start, end, 1);

	}

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
